package sv.gestionacademica.service;

import sv.gestionacademica.entity.EstadoCuenta;
import sv.gestionacademica.entity.Pago;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.repository.PagoRepository;
import sv.gestionacademica.repository.UsuarioRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Estado de cuenta de los estudiantes.
 *
 * Con el constructor que recibe repositorios, el estado se calcula desde la BD
 * (tabla pago, que llena el flujo matricula -> pago -> factura).
 * El constructor sin argumentos se mantiene para las pruebas en memoria.
 */
public class EstadoCuentaService {

    /** Dias que puede estar pendiente un pago antes de considerar al estudiante en mora. */
    public static final int DIAS_GRACIA_POR_DEFECTO = 30;

    private final List<EstadoCuenta> listaCuentas = new ArrayList<>();
    private final PagoRepository pagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final int diasGracia;

    public EstadoCuentaService() {
        this(null, null, DIAS_GRACIA_POR_DEFECTO);
    }

    public EstadoCuentaService(PagoRepository pagoRepository, UsuarioRepository usuarioRepository) {
        this(pagoRepository, usuarioRepository, DIAS_GRACIA_POR_DEFECTO);
    }

    public EstadoCuentaService(PagoRepository pagoRepository, UsuarioRepository usuarioRepository, int diasGracia) {
        this.pagoRepository = pagoRepository;
        this.usuarioRepository = usuarioRepository;
        this.diasGracia = diasGracia;
    }

    public void registrarEstadoCuenta(EstadoCuenta cuenta) {
        listaCuentas.removeIf(c -> c.getIdEstudiante().equals(cuenta.getIdEstudiante()));
        listaCuentas.add(cuenta);
    }

    public EstadoCuenta consultarEstadoCuenta(String idEstudiante) {
        for (EstadoCuenta cuenta : listaCuentas) {
            if (cuenta.getIdEstudiante().equalsIgnoreCase(idEstudiante)) {
                return cuenta;
            }
        }
        return null;
    }

    public List<EstadoCuenta> obtenerEstudiantesEnMora() {
        List<EstadoCuenta> enMoraList = new ArrayList<>();
        for (EstadoCuenta cuenta : listaCuentas) {
            if (cuenta.isEnMora()) {
                enMoraList.add(cuenta);
            }
        }
        return enMoraList;
    }

    // ------------------------------------------------------------------
    // Calculo desde la BD
    // ------------------------------------------------------------------

    /**
     * Calcula el estado de cuenta de un estudiante a partir de sus pagos en la BD
     * y lo deja registrado (para que obtenerEstudiantesEnMora lo considere).
     *
     * saldoPendiente = suma de pagos PENDIENTE.
     * enMora = tiene algun pago PENDIENTE con mas de diasGracia dias.
     */
    public EstadoCuenta calcularDesdeBD(int idEstudiante) {
        exigirRepositorios();
        Usuario estudiante = usuarioRepository.buscarPorId(idEstudiante);
        if (estudiante == null) {
            throw new IllegalArgumentException("No existe el estudiante con id " + idEstudiante);
        }
        EstadoCuenta cuenta = calcular(estudiante);
        registrarEstadoCuenta(cuenta);
        return cuenta;
    }

    /** Recalcula el estado de cuenta de todos los estudiantes desde la BD. */
    public List<EstadoCuenta> calcularTodosDesdeBD() {
        exigirRepositorios();
        List<EstadoCuenta> cuentas = new ArrayList<>();
        for (Usuario u : usuarioRepository.listarTodos()) {
            if (u.getRol() != null && "Estudiante".equalsIgnoreCase(u.getRol().getNombreRol())) {
                EstadoCuenta cuenta = calcular(u);
                registrarEstadoCuenta(cuenta);
                cuentas.add(cuenta);
            }
        }
        return cuentas;
    }

    /** Suma de pagos APROBADO del estudiante (lo que ya tiene factura). */
    public double totalPagado(int idEstudiante) {
        exigirRepositorios();
        double total = 0;
        for (Pago p : pagoRepository.listarPorEstudiante(String.valueOf(idEstudiante))) {
            if (p.estaAprobado()) {
                total += p.getMonto();
            }
        }
        return total;
    }

    public int getDiasGracia() {
        return diasGracia;
    }

    private EstadoCuenta calcular(Usuario estudiante) {
        String id = String.valueOf(estudiante.getIdUsuario());
        LocalDate limite = LocalDate.now().minusDays(diasGracia);

        double saldoPendiente = 0;
        boolean enMora = false;
        for (Pago p : pagoRepository.listarPorEstudiante(id)) {
            if (p.estaPendiente()) {
                saldoPendiente += p.getMonto();
                if (p.getFechaPago() != null && p.getFechaPago().isBefore(limite)) {
                    enMora = true;
                }
            }
        }
        String nombre = estudiante.getNombreUsuario() + " " + estudiante.getApellidoUsuario();
        return new EstadoCuenta(id, nombre, saldoPendiente, enMora);
    }

    private void exigirRepositorios() {
        if (pagoRepository == null || usuarioRepository == null) {
            throw new IllegalStateException(
                    "EstadoCuentaService se creó sin repositorios; usa el constructor con PagoRepository y UsuarioRepository.");
        }
    }
}
