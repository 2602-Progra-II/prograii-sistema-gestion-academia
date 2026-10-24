package sv.gestionacademica.menu;

import sv.gestionacademica.entity.EstadoCuenta;
import sv.gestionacademica.entity.Factura;
import sv.gestionacademica.entity.IndicadorMora;
import sv.gestionacademica.entity.Pago;
import sv.gestionacademica.entity.PeriodoAcademico;
import sv.gestionacademica.entity.Sesion;
import sv.gestionacademica.repository.FacturaRepository;
import sv.gestionacademica.repository.PagoRepository;
import sv.gestionacademica.repository.PeriodoAcademicoRepository;
import sv.gestionacademica.service.EstadoCuentaService;
import sv.gestionacademica.service.ReporteService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Reportes financieros y estado de cuenta, calculados desde las tablas pago y factura
 * (las que llena el flujo matricula -> pago -> factura).
 */
public class MenuReportes implements ModuloMenu {

    private final Consola consola;
    private final EstadoCuentaService estadoCuentaService;
    private final ReporteService reporteService;
    private final PagoRepository pagoRepository;
    private final FacturaRepository facturaRepository;
    private final PeriodoAcademicoRepository periodoRepository;

    public MenuReportes(Consola consola, EstadoCuentaService estadoCuentaService, ReporteService reporteService,
                        PagoRepository pagoRepository, FacturaRepository facturaRepository,
                        PeriodoAcademicoRepository periodoRepository) {
        this.consola = consola;
        this.estadoCuentaService = estadoCuentaService;
        this.reporteService = reporteService;
        this.pagoRepository = pagoRepository;
        this.facturaRepository = facturaRepository;
        this.periodoRepository = periodoRepository;
    }

    /** Menu completo (Administrador). */
    @Override
    public void ejecutar(Sesion sesion) {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n===== REPORTES Y ESTADO DE CUENTA =====");
            System.out.println("1. Estado de cuenta de un estudiante");
            System.out.println("2. Estado de cuenta de todos los estudiantes");
            System.out.println("3. Estudiantes en mora + indicador de mora");
            System.out.println("4. Facturas emitidas");
            System.out.println("0. Volver");
            String opcion = consola.leerTexto("Opción: ");
            try {
                switch (opcion) {
                    case "1" -> mostrarEstadoCuenta(consola.leerEntero("Id del estudiante: "));
                    case "2" -> listarTodos();
                    case "3" -> reporteMora();
                    case "4" -> listarFacturas();
                    case "0" -> volver = true;
                    default -> System.out.println("Opción no válida.");
                }
            } catch (RuntimeException e) {
                System.err.println("[Error] " + e.getMessage());
            }
        }
    }

    /** Lo usa tambien el menu del Estudiante para ver SU estado de cuenta. */
    public void mostrarEstadoCuenta(int idEstudiante) {
        EstadoCuenta cuenta = estadoCuentaService.calcularDesdeBD(idEstudiante);
        System.out.println("\n--- Estado de cuenta: " + cuenta.getNombreEstudiante() + " (id " + idEstudiante + ") ---");
        for (Pago p : pagoRepository.listarPorEstudiante(String.valueOf(idEstudiante))) {
            System.out.println("  Pago [" + p.getIdPago() + "] matrícula " + p.getIdMatricula()
                    + " | $" + String.format("%.2f", p.getMonto()) + " | " + p.getEstadoPago() + " | " + p.getFechaPago());
        }
        System.out.println("  Total pagado:    $" + String.format("%.2f", estadoCuentaService.totalPagado(idEstudiante)));
        System.out.println("  Saldo pendiente: $" + String.format("%.2f", cuenta.getSaldoPendiente()));
        System.out.println("  En mora:         " + (cuenta.isEnMora() ? "SÍ" : "No")
                + " (pago pendiente con más de " + estadoCuentaService.getDiasGracia() + " días)");
    }

    private void listarTodos() {
        System.out.println("\n--- Estado de cuenta de estudiantes ---");
        for (EstadoCuenta c : estadoCuentaService.calcularTodosDesdeBD()) {
            System.out.println("  [" + c.getIdEstudiante() + "] " + c.getNombreEstudiante()
                    + " | pendiente $" + String.format("%.2f", c.getSaldoPendiente())
                    + (c.isEnMora() ? " | EN MORA" : ""));
        }
    }

    private void reporteMora() {
        List<EstadoCuenta> cuentas = estadoCuentaService.calcularTodosDesdeBD();
        List<EstadoCuenta> enMora = estadoCuentaService.obtenerEstudiantesEnMora();

        System.out.println("\n--- Estudiantes en mora ---");
        if (enMora.isEmpty()) {
            System.out.println("  (ninguno)");
        }
        for (EstadoCuenta c : enMora) {
            System.out.println("  [" + c.getIdEstudiante() + "] " + c.getNombreEstudiante()
                    + " | $" + String.format("%.2f", c.getSaldoPendiente()));
        }

        IndicadorMora indicador = reporteService.generarIndicadorMora(cuentas, periodosVigentes());
        System.out.println("\n--- Indicador de mora ---");
        System.out.println("  Período(s):            " + indicador.getPeriodoAcademico());
        System.out.println("  Estudiantes en mora:   " + indicador.getTotalEstudiantesEnMora() + " de " + cuentas.size());
        System.out.println("  Monto total en mora:   $" + String.format("%.2f", indicador.getMontoTotalPendiente()));
    }

    private void listarFacturas() {
        System.out.println("\n--- Facturas emitidas ---");
        List<Factura> facturas = facturaRepository.listarTodas();
        double total = 0;
        for (Factura f : facturas) {
            System.out.println("  " + f.getNumeroFactura() + " | " + f.getFechaEmision()
                    + " | $" + String.format("%.2f", f.getMontoTotal()) + " | pago " + f.getIdPago());
            total += f.getMontoTotal();
        }
        System.out.println("  Total facturado: $" + String.format("%.2f", total) + " (" + facturas.size() + " facturas)");
    }

    private String periodosVigentes() {
        String nombres = periodoRepository.listarVigentes().stream()
                .map(PeriodoAcademico::getNombrePeriodo)
                .collect(Collectors.joining(", "));
        return nombres.isEmpty() ? "Sin período activo" : nombres;
    }
}
