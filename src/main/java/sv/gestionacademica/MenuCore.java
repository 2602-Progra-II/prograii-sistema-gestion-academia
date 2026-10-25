package sv.gestionacademica;

import sv.gestionacademica.controller.MatriculaController;
import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.entity.Matricula;
import sv.gestionacademica.entity.Pago;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.repository.CursoRepository;
import sv.gestionacademica.repository.MatriculaRepository;
import sv.gestionacademica.repository.PagoRepository;
import sv.gestionacademica.repository.UsuarioRepository;

import java.util.List;
import java.util.Scanner;

/**
 * Menu interactivo de consola para el flujo transaccional core
 * (matricula -> pago -> factura). Todos los datos se piden al usuario
 * y se consultan en la BD; no hay ids ni montos fijos en el codigo.
 *
 * Se lanza desde Main, que arma las dependencias (repositories -> service -> controller).
 */
public class MenuCore {

    private static final String ASESOR_POR_DEFECTO = "1"; // administrador de data.sql

    private final Scanner in;
    /** Id del asesor tomado de la sesion; si es null se pregunta por consola (modo sin login). */
    private final String idAsesorSesion;
    private final MatriculaController controller;
    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final PagoRepository pagoRepository;
    private final MatriculaRepository matriculaRepository;

    public MenuCore(MatriculaController controller,
                    UsuarioRepository usuarioRepository,
                    CursoRepository cursoRepository,
                    PagoRepository pagoRepository,
                    MatriculaRepository matriculaRepository) {
        this(controller, usuarioRepository, cursoRepository, pagoRepository, matriculaRepository,
                new Scanner(System.in), null);
    }

    /**
     * Version usada desde el menu principal: comparte el Scanner de la aplicacion
     * y toma el asesor (quien aprueba/rechaza pagos) del usuario con sesion iniciada.
     */
    public MenuCore(MatriculaController controller,
                    UsuarioRepository usuarioRepository,
                    CursoRepository cursoRepository,
                    PagoRepository pagoRepository,
                    MatriculaRepository matriculaRepository,
                    Scanner in,
                    String idAsesorSesion) {
        this.in = in;
        this.idAsesorSesion = idAsesorSesion;
        this.controller = controller;
        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
        this.pagoRepository = pagoRepository;
        this.matriculaRepository = matriculaRepository;
    }

    public void ejecutar() {
        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            String opcion = in.nextLine().trim();
            try {
                switch (opcion) {
                    case "1" -> listarEstudiantes();
                    case "2" -> listarCursos();
                    case "3" -> matricular();
                    case "4" -> listarPagosPendientes();
                    case "5" -> aprobarPago();
                    case "6" -> rechazarPago();
                    case "7" -> verMatriculasDeEstudiante();
                    case "0" -> salir = true;
                    default -> System.out.println("Opción no válida.");
                }
            } catch (RuntimeException e) {
                System.err.println("[Error inesperado] " + e.getMessage());
            }
        }
    }

    private void mostrarMenu() {
        System.out.println("\n===== FLUJO CORE: MATRÍCULA - PAGO - FACTURA =====");
        System.out.println("1. Ver estudiantes");
        System.out.println("2. Ver cursos");
        System.out.println("3. Matricular estudiante (crea matrícula + pago pendiente)");
        System.out.println("4. Ver pagos pendientes");
        System.out.println("5. Aprobar pago (activa matrícula + emite factura)");
        System.out.println("6. Rechazar pago (cancela matrícula)");
        System.out.println("7. Ver matrículas de un estudiante");
        System.out.println("0. Volver");
        System.out.print("Opción: ");
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    private void listarEstudiantes() {
        System.out.println("\n--- Estudiantes ---");
        boolean hay = false;
        for (Usuario u : usuarioRepository.listarTodos()) {
            if (u.getRol() != null && "Estudiante".equalsIgnoreCase(u.getRol().getNombreRol())) {
                System.out.println("  [" + u.getIdUsuario() + "] " + u.getNombreUsuario() + " "
                        + u.getApellidoUsuario() + " (" + u.getEstado() + ")");
                hay = true;
            }
        }
        if (!hay) {
            System.out.println("  (no hay estudiantes)");
        }
    }

    private void listarCursos() {
        System.out.println("\n--- Cursos ---");
        List<Curso> lista = cursoRepository.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("  (no hay cursos)");
        }
        for (Curso c : lista) {
            System.out.println("  [" + c.getIdCurso() + "] " + c.getCodigoCurso() + " - " + c.getNombreCurso());
        }
    }

    private void listarPagosPendientes() {
        System.out.println("\n--- Pagos pendientes ---");
        List<Pago> lista = pagoRepository.listarPendientes();
        if (lista.isEmpty()) {
            System.out.println("  (no hay pagos pendientes)");
        }
        for (Pago p : lista) {
            System.out.println("  Pago [" + p.getIdPago() + "] estudiante " + p.getIdEstudiante()
                    + " | matrícula " + p.getIdMatricula() + " | $" + p.getMonto());
        }
    }

    private void verMatriculasDeEstudiante() {
        int idEstudiante = leerEntero("Id del estudiante: ");
        List<Matricula> lista = matriculaRepository.findByEstudiante(String.valueOf(idEstudiante));
        System.out.println("\n--- Matrículas del estudiante " + idEstudiante + " ---");
        if (lista.isEmpty()) {
            System.out.println("  (sin matrículas)");
        }
        for (Matricula m : lista) {
            String curso = m.getCurso() != null ? m.getCurso().getNombreCurso() : "?";
            System.out.println("  Matrícula [" + m.getIdMatricula() + "] " + curso
                    + " - " + m.getEstadoMatricula());
        }
    }

    // ------------------------------------------------------------------
    // Operaciones transaccionales
    // ------------------------------------------------------------------

    private void matricular() {
        listarEstudiantes();
        int idEstudiante = leerEntero("Id del estudiante: ");
        listarCursos();
        int idCurso = leerEntero("Id del curso: ");
        double monto = leerDecimal("Monto a pagar: ");
        controller.matricularEstudiante(idEstudiante, idCurso, monto);
    }

    private void aprobarPago() {
        listarPagosPendientes();
        int idPago = leerEntero("Id del pago a aprobar: ");
        controller.aprobarPago(idPago, leerAsesor());
    }

    private void rechazarPago() {
        listarPagosPendientes();
        int idPago = leerEntero("Id del pago a rechazar: ");
        controller.rechazarPago(idPago, leerAsesor());
    }

    // ------------------------------------------------------------------
    // Lectura segura de datos
    // ------------------------------------------------------------------

    private String leerAsesor() {
        if (idAsesorSesion != null) {
            return idAsesorSesion;
        }
        System.out.print("Id del asesor [" + ASESOR_POR_DEFECTO + "]: ");
        String texto = in.nextLine().trim();
        return texto.isEmpty() ? ASESOR_POR_DEFECTO : texto;
    }

    private int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Integer.parseInt(in.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un número entero válido.");
            }
        }
    }

    private double leerDecimal(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Double.parseDouble(in.nextLine().trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un monto válido (ejemplo: 150.00).");
            }
        }
    }
}
