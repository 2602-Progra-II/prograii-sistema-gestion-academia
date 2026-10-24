package sv.gestionacademica.service;

import sv.gestionacademica.dto.ResultadoMatricula;
import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.entity.Factura;
import sv.gestionacademica.entity.Matricula;
import sv.gestionacademica.entity.Pago;
import sv.gestionacademica.entity.PeriodoAcademico;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.enums.EstadoMatricula;
import sv.gestionacademica.enums.EstadoPago;
import sv.gestionacademica.enums.EstadoPeriodo;
import sv.gestionacademica.enums.EstadoUsuario;
import sv.gestionacademica.exception.BusinessException;
import sv.gestionacademica.exception.TransaccionException;
import sv.gestionacademica.repository.CursoRepository;
import sv.gestionacademica.repository.FacturaRepository;
import sv.gestionacademica.repository.MatriculaRepository;
import sv.gestionacademica.repository.PagoRepository;
import sv.gestionacademica.repository.PeriodoAcademicoRepository;
import sv.gestionacademica.repository.UsuarioRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Flujo transaccional core: matricula -> pago -> factura.
 *
 * Cada operacion publica es ATOMICA: usa UNA sola Connection del pool para todos sus pasos,
 * y termina con commit (todo se guarda) o rollback (no queda nada a medias).
 *
 *  - matricular():     crea la matricula (PENDIENTE_PAGO) y su pago (PENDIENTE).
 *  - aprobarPago():    pago APROBADO + matricula ACTIVA + factura emitida.
 *  - rechazarPago():   pago RECHAZADO + matricula CANCELADA.
 *
 * Las reglas de negocio incumplidas lanzan BusinessException; los fallos tecnicos, TransaccionException.
 */
public class FlujoMatriculaService {

    private static final String ROL_ESTUDIANTE = "Estudiante";
    // Codigos de SQL Server para "clave duplicada" (indice unico / restriccion unica)
    private static final int ERROR_INDICE_UNICO = 2601;
    private static final int ERROR_RESTRICCION_UNICA = 2627;

    private final DataSource dataSource;
    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final PeriodoAcademicoRepository periodoRepository;
    private final MatriculaRepository matriculaRepository;
    private final PagoRepository pagoRepository;
    private final FacturaRepository facturaRepository;

    public FlujoMatriculaService(DataSource dataSource,
                                 UsuarioRepository usuarioRepository,
                                 CursoRepository cursoRepository,
                                 PeriodoAcademicoRepository periodoRepository,
                                 MatriculaRepository matriculaRepository,
                                 PagoRepository pagoRepository,
                                 FacturaRepository facturaRepository) {
        this.dataSource = dataSource;
        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
        this.periodoRepository = periodoRepository;
        this.matriculaRepository = matriculaRepository;
        this.pagoRepository = pagoRepository;
        this.facturaRepository = facturaRepository;
    }

    // ------------------------------------------------------------------
    // 1. Matricular
    // ------------------------------------------------------------------

    public ResultadoMatricula matricular(int idEstudiante, int idCurso, double monto) {
        if (monto <= 0) {
            throw new BusinessException("El monto de la matrícula debe ser mayor a cero.");
        }

        // Validaciones de lectura (no necesitan transaccion): se hacen antes de abrirla.
        Usuario estudiante = validarEstudiante(idEstudiante);
        Curso curso = cursoRepository.buscarPorId(idCurso)
                .orElseThrow(() -> new BusinessException("No existe el curso con id " + idCurso + "."));
        validarPeriodoDelCurso(curso);

        return enTransaccion(con -> {
            if (tieneMatriculaVigente(con, idEstudiante, idCurso)) {
                throw new BusinessException("El estudiante ya está matriculado en este curso.");
            }

            Matricula matricula = new Matricula(0, LocalDate.now(), EstadoMatricula.PENDIENTE_PAGO, estudiante, curso);
            matriculaRepository.save(con, matricula);

            Pago pago = new Pago(0, monto, LocalDate.now(), null, String.valueOf(idEstudiante));
            pago.setEstadoPago(EstadoPago.PENDIENTE);
            pago.setIdMatricula(matricula.getIdMatricula());
            pagoRepository.guardar(con, pago);

            return new ResultadoMatricula(matricula, pago);
        });
    }

    // ------------------------------------------------------------------
    // 2. Aprobar pago -> activar matricula -> emitir factura
    // ------------------------------------------------------------------

    public Factura aprobarPago(int idPago, String idAsesor) {
        validarAsesor(idAsesor);

        return enTransaccion(con -> {
            Pago pago = obtenerPagoPendienteBloqueado(con, idPago);
            Matricula matricula = obtenerMatriculaDelPago(con, pago);

            if (matricula.getEstadoMatricula() == EstadoMatricula.CANCELADA) {
                throw new BusinessException("No se puede aprobar el pago: la matrícula está cancelada.");
            }

            pago.setEstadoPago(EstadoPago.APROBADO);
            pago.setIdAsesorValidador(idAsesor);
            pagoRepository.actualizar(con, pago);

            matricula.setEstadoMatricula(EstadoMatricula.ACTIVA);
            matriculaRepository.update(con, matricula);

            Factura factura = new Factura(0, generarNumeroFactura(pago), LocalDate.now(), pago.getMonto());
            factura.setIdPago(pago.getIdPago());
            facturaRepository.guardar(con, factura);

            return factura;
        });
    }

    // ------------------------------------------------------------------
    // 3. Rechazar pago -> cancelar matricula
    // ------------------------------------------------------------------

    public void rechazarPago(int idPago, String idAsesor) {
        validarAsesor(idAsesor);

        enTransaccion(con -> {
            Pago pago = obtenerPagoPendienteBloqueado(con, idPago);
            Matricula matricula = obtenerMatriculaDelPago(con, pago);

            pago.setEstadoPago(EstadoPago.RECHAZADO);
            pago.setIdAsesorValidador(idAsesor);
            pagoRepository.actualizar(con, pago);

            matricula.setEstadoMatricula(EstadoMatricula.CANCELADA);
            matriculaRepository.update(con, matricula);

            return null;
        });
    }

    // ------------------------------------------------------------------
    // Validaciones de negocio
    // ------------------------------------------------------------------

    private Usuario validarEstudiante(int idEstudiante) {
        Usuario usuario = usuarioRepository.buscarPorId(idEstudiante);
        if (usuario == null) {
            throw new BusinessException("No existe el usuario con id " + idEstudiante + ".");
        }
        if (usuario.getRol() == null || !ROL_ESTUDIANTE.equalsIgnoreCase(usuario.getRol().getNombreRol())) {
            throw new BusinessException("El usuario " + idEstudiante + " no tiene rol de Estudiante.");
        }
        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new BusinessException("El estudiante " + idEstudiante + " está inactivo.");
        }
        return usuario;
    }

    /** Regla academica: no se puede matricular en un curso cuyo periodo ya finalizo. */
    private void validarPeriodoDelCurso(Curso curso) {
        PeriodoAcademico periodo = periodoRepository.buscarPorId(curso.getIdPeriodo())
                .orElseThrow(() -> new BusinessException(
                        "El curso " + curso.getIdCurso() + " no tiene un periodo académico válido."));
        if (periodo.getEstado() == EstadoPeriodo.FINALIZADO) {
            throw new BusinessException("No se puede matricular: el periodo \""
                    + periodo.getNombrePeriodo() + "\" ya finalizó.");
        }
    }

    private void validarAsesor(String idAsesor) {
        if (idAsesor == null || idAsesor.isBlank()) {
            throw new BusinessException("Debe indicarse quién valida el pago.");
        }
    }

    private boolean tieneMatriculaVigente(Connection con, int idEstudiante, int idCurso) {
        for (Matricula m : matriculaRepository.findByEstudiante(con, String.valueOf(idEstudiante))) {
            boolean mismoCurso = m.getCurso() != null && m.getCurso().getIdCurso() == idCurso;
            if (mismoCurso && m.getEstadoMatricula() != EstadoMatricula.CANCELADA) {
                return true;
            }
        }
        return false;
    }

    private Pago obtenerPagoPendienteBloqueado(Connection con, int idPago) {
        Pago pago = pagoRepository.buscarPorIdBloqueando(con, idPago)
                .orElseThrow(() -> new BusinessException("No existe el pago con id " + idPago + "."));
        if (!pago.estaPendiente()) {
            throw new BusinessException("El pago ya fue procesado anteriormente.");
        }
        return pago;
    }

    private Matricula obtenerMatriculaDelPago(Connection con, Pago pago) {
        if (pago.getIdMatricula() == null) {
            throw new BusinessException("El pago no está asociado a ninguna matrícula.");
        }
        return matriculaRepository.findById(con, pago.getIdMatricula())
                .orElseThrow(() -> new BusinessException(
                        "No existe la matrícula " + pago.getIdMatricula() + " asociada al pago."));
    }

    private String generarNumeroFactura(Pago pago) {
        // Una factura por pago: el id del pago garantiza que el numero sea unico.
        return String.format("FAC-%d-%06d", LocalDate.now().getYear(), pago.getIdPago());
    }

    // ------------------------------------------------------------------
    // Manejo de la transaccion (JDBC)
    // ------------------------------------------------------------------

    @FunctionalInterface
    private interface TrabajoTransaccional<T> {
        T ejecutar(Connection con);
    }

    private <T> T enTransaccion(TrabajoTransaccional<T> trabajo) {
        try (Connection con = dataSource.getConnection()) {
            boolean autoCommitOriginal = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                T resultado = trabajo.ejecutar(con);
                con.commit();
                return resultado;
            } catch (SQLException e) {
                rollbackSilencioso(con, e);
                throw new TransaccionException("Error de base de datos al confirmar la transacción.", e);
            } catch (RuntimeException e) {
                rollbackSilencioso(con, e);
                throw traducir(e);
            } finally {
                restaurarAutoCommit(con, autoCommitOriginal);
            }
        } catch (SQLException e) {
            throw new TransaccionException("No se pudo obtener o cerrar la conexión con la base de datos.", e);
        }
    }

    private void rollbackSilencioso(Connection con, Exception causa) {
        try {
            con.rollback();
        } catch (SQLException ex) {
            causa.addSuppressed(ex);
        }
    }

    private void restaurarAutoCommit(Connection con, boolean valorOriginal) {
        try {
            con.setAutoCommit(valorOriginal);
        } catch (SQLException ignorada) {
            // la conexion vuelve al pool, que la restablece por su cuenta
        }
    }

    private RuntimeException traducir(RuntimeException e) {
        if (e instanceof BusinessException || e instanceof TransaccionException) {
            return e;
        }
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof SQLException sql
                    && (sql.getErrorCode() == ERROR_INDICE_UNICO || sql.getErrorCode() == ERROR_RESTRICCION_UNICA)) {
                // Dos matriculas simultaneas del mismo estudiante/curso: gana una, la otra cae aqui.
                return new BusinessException("El estudiante ya está matriculado en este curso.");
            }
        }
        return new TransaccionException("Error técnico durante la transacción: " + e.getMessage(), e);
    }
}
