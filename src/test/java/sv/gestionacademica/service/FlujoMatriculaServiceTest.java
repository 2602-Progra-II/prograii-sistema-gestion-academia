package sv.gestionacademica.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sv.gestionacademica.dto.ResultadoMatricula;
import sv.gestionacademica.entity.Factura;
import sv.gestionacademica.entity.Pago;
import sv.gestionacademica.enums.EstadoMatricula;
import sv.gestionacademica.enums.EstadoPago;
import sv.gestionacademica.exception.BusinessException;
import sv.gestionacademica.exception.TransaccionException;
import sv.gestionacademica.persistence.DataSourceConfig;
import sv.gestionacademica.repository.CursoRepositoryImpl;
import sv.gestionacademica.repository.FacturaRepositoryImpl;
import sv.gestionacademica.repository.MatriculaRepository;
import sv.gestionacademica.repository.MatriculaRepositoryImpl;
import sv.gestionacademica.repository.PagoRepository;
import sv.gestionacademica.repository.PagoRepositoryImpl;
import sv.gestionacademica.repository.PeriodoAcademicoRepositoryImpl;
import sv.gestionacademica.repository.UsuarioRepositoryImpl;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de INTEGRACION del flujo transaccional (matricula -> pago -> factura) contra SQL Server.
 *
 * Requisitos: BD creada con database/schema.sql + database/data.sql y variables DB_URL, DB_USER, DB_PASSWORD.
 * Usa el estudiante 4 y el curso 2 de data.sql, y limpia SOLO las filas que ella misma crea
 * (no borra el resto de la base).
 */
class FlujoMatriculaServiceTest {

    private static final int ESTUDIANTE = 4;   // Fransheska (rol Estudiante, activo)
    private static final int CURSO = 2;        // Calculo Diferencial (sin matricula previa del estudiante 4)
    private static final double MONTO = 150.0;

    private MatriculaRepository matriculaRepository;
    private PagoRepository pagoRepository;
    private FlujoMatriculaService servicio;

    @BeforeEach
    void setUp() {
        matriculaRepository = new MatriculaRepositoryImpl();
        pagoRepository = new PagoRepositoryImpl();
        servicio = crearServicio(new FacturaRepositoryImpl());
        limpiarDatosDeLaPrueba();
    }

    @AfterEach
    void tearDown() {
        limpiarDatosDeLaPrueba();
    }

    private FlujoMatriculaService crearServicio(sv.gestionacademica.repository.FacturaRepository facturaRepository) {
        return new FlujoMatriculaService(DataSourceConfig.getDataSource(), new UsuarioRepositoryImpl(),
                new CursoRepositoryImpl(), new PeriodoAcademicoRepositoryImpl(), matriculaRepository, pagoRepository, facturaRepository);
    }

    @Test
    @DisplayName("Matricular crea la matricula PENDIENTE_PAGO y su pago PENDIENTE")
    void matricularCreaMatriculaYPagoPendientes() {
        ResultadoMatricula r = servicio.matricular(ESTUDIANTE, CURSO, MONTO);

        assertTrue(r.matricula().getIdMatricula() > 0);
        assertEquals(EstadoMatricula.PENDIENTE_PAGO,
                matriculaRepository.findById(r.matricula().getIdMatricula()).orElseThrow().getEstadoMatricula());

        Pago pago = pagoRepository.buscarPorId(r.pago().getIdPago()).orElseThrow();
        assertEquals(EstadoPago.PENDIENTE, pago.getEstadoPago());
        assertEquals(r.matricula().getIdMatricula(), pago.getIdMatricula());
    }

    @Test
    @DisplayName("Aprobar el pago activa la matricula y emite la factura")
    void aprobarPagoActivaMatriculaYEmiteFactura() {
        ResultadoMatricula r = servicio.matricular(ESTUDIANTE, CURSO, MONTO);

        Factura factura = servicio.aprobarPago(r.pago().getIdPago(), "1");

        assertTrue(factura.getIdFactura() > 0);
        assertEquals(r.pago().getIdPago(), factura.getIdPago());
        assertEquals(EstadoPago.APROBADO, pagoRepository.buscarPorId(r.pago().getIdPago()).orElseThrow().getEstadoPago());
        assertEquals(EstadoMatricula.ACTIVA,
                matriculaRepository.findById(r.matricula().getIdMatricula()).orElseThrow().getEstadoMatricula());
    }

    @Test
    @DisplayName("Rechazar el pago cancela la matricula")
    void rechazarPagoCancelaMatricula() {
        ResultadoMatricula r = servicio.matricular(ESTUDIANTE, CURSO, MONTO);

        servicio.rechazarPago(r.pago().getIdPago(), "1");

        assertEquals(EstadoPago.RECHAZADO, pagoRepository.buscarPorId(r.pago().getIdPago()).orElseThrow().getEstadoPago());
        assertEquals(EstadoMatricula.CANCELADA,
                matriculaRepository.findById(r.matricula().getIdMatricula()).orElseThrow().getEstadoMatricula());
    }

    @Test
    @DisplayName("ROLLBACK: si falla la factura, el pago y la matricula NO cambian")
    void siFallaLaFacturaNadaQuedaAMedias() {
        ResultadoMatricula r = servicio.matricular(ESTUDIANTE, CURSO, MONTO);

        // Factura "rota": falla justo en el ultimo paso de aprobarPago, cuando pago y matricula ya fueron actualizados.
        FlujoMatriculaService servicioConFallo = crearServicio(new FacturaRepositoryImpl() {
            @Override
            public void guardar(Connection con, Factura factura) {
                throw new RuntimeException("Fallo simulado al guardar la factura");
            }
        });

        assertThrows(TransaccionException.class, () -> servicioConFallo.aprobarPago(r.pago().getIdPago(), "1"));

        assertEquals(EstadoPago.PENDIENTE, pagoRepository.buscarPorId(r.pago().getIdPago()).orElseThrow().getEstadoPago(),
                "El pago debe seguir PENDIENTE tras el rollback");
        assertEquals(EstadoMatricula.PENDIENTE_PAGO,
                matriculaRepository.findById(r.matricula().getIdMatricula()).orElseThrow().getEstadoMatricula(),
                "La matricula debe seguir PENDIENTE_PAGO tras el rollback");
    }

    @Test
    @DisplayName("No se puede matricular dos veces al mismo estudiante en el mismo curso")
    void noPermiteMatriculaDuplicada() {
        servicio.matricular(ESTUDIANTE, CURSO, MONTO);
        assertThrows(BusinessException.class, () -> servicio.matricular(ESTUDIANTE, CURSO, MONTO));
    }

    @Test
    @DisplayName("No se puede aprobar dos veces el mismo pago")
    void noPermiteAprobarDosVeces() {
        ResultadoMatricula r = servicio.matricular(ESTUDIANTE, CURSO, MONTO);
        servicio.aprobarPago(r.pago().getIdPago(), "1");
        assertThrows(BusinessException.class, () -> servicio.aprobarPago(r.pago().getIdPago(), "1"));
    }

    @Test
    @DisplayName("Reglas de negocio: estudiante, curso, rol y monto invalidos")
    void validacionesDeNegocio() {
        assertThrows(BusinessException.class, () -> servicio.matricular(999, CURSO, MONTO));
        assertThrows(BusinessException.class, () -> servicio.matricular(ESTUDIANTE, 999, MONTO));
        assertThrows(BusinessException.class, () -> servicio.matricular(2, CURSO, MONTO)); // 2 = profesor
        assertThrows(BusinessException.class, () -> servicio.matricular(ESTUDIANTE, CURSO, 0));
    }

    @Test
    @DisplayName("Regla academica: no se puede matricular en un curso de un periodo FINALIZADO")
    void noMatriculaEnPeriodoFinalizado() throws Exception {
        int idPeriodo;
        int idCurso;
        try (Connection con = DataSourceConfig.getConnection()) {
            try (java.sql.PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO periodo_academico (nombre_periodo, fecha_inicio, fecha_fin, estado) "
                            + "VALUES ('TEST-FINALIZADO', '2020-01-01', '2020-06-30', 'FINALIZADO')",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.executeUpdate();
                try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    idPeriodo = rs.getInt(1);
                }
            }
            try (java.sql.PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO curso (nombre_curso, codigo_curso, descripcion, id_periodo) "
                            + "VALUES ('Curso de prueba', 'TEST-FIN', 'Prueba', ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idPeriodo);
                ps.executeUpdate();
                try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    idCurso = rs.getInt(1);
                }
            }
        }

        try {
            assertThrows(BusinessException.class, () -> servicio.matricular(ESTUDIANTE, idCurso, MONTO));
        } finally {
            try (Connection con = DataSourceConfig.getConnection(); Statement st = con.createStatement()) {
                st.executeUpdate("DELETE FROM curso WHERE id_curso = " + idCurso);
                st.executeUpdate("DELETE FROM periodo_academico WHERE id_periodo = " + idPeriodo);
            }
        }
    }

    /** Borra unicamente lo que crea esta prueba: matriculas del estudiante 4 en el curso 2 y sus pagos/facturas. */
    private void limpiarDatosDeLaPrueba() {
        String matriculas = "(SELECT id_matricula FROM matricula WHERE id_estudiante = '" + ESTUDIANTE + "' AND id_curso = " + CURSO + ")";
        String pagos = "(SELECT id_pago FROM pago WHERE id_matricula IN " + matriculas + ")";
        try (Connection con = DataSourceConfig.getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("DELETE FROM factura WHERE id_pago IN " + pagos);
            st.executeUpdate("DELETE FROM pago WHERE id_matricula IN " + matriculas);
            st.executeUpdate("DELETE FROM matricula WHERE id_estudiante = '" + ESTUDIANTE + "' AND id_curso = " + CURSO);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo limpiar los datos de la prueba (¿BD sin schema/data?)", e);
        }
    }
}
