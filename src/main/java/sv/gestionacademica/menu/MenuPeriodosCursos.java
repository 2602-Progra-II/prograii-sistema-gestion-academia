package sv.gestionacademica.menu;

import sv.gestionacademica.controller.AcademicoController;
import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.entity.PeriodoAcademico;
import sv.gestionacademica.entity.Sesion;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.repository.CursoRepository;
import sv.gestionacademica.repository.UsuarioRepository;

import java.time.LocalDate;
import java.util.List;

/** Periodos academicos y cursos (solo Administrador). Usa AcademicoController. */
public class MenuPeriodosCursos implements ModuloMenu {

    private final Consola consola;
    private final AcademicoController controller;
    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;

    public MenuPeriodosCursos(Consola consola, AcademicoController controller,
                              CursoRepository cursoRepository, UsuarioRepository usuarioRepository) {
        this.consola = consola;
        this.controller = controller;
        this.cursoRepository = cursoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void ejecutar(Sesion sesion) {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n===== PERÍODOS Y CURSOS =====");
            System.out.println("1. Ver períodos");
            System.out.println("2. Registrar período");
            System.out.println("3. Renombrar período");
            System.out.println("4. Activar período");
            System.out.println("5. Finalizar período");
            System.out.println("6. Ver todos los cursos");
            System.out.println("7. Ver cursos de un período");
            System.out.println("8. Registrar curso");
            System.out.println("9. Activar / desactivar curso");
            System.out.println("10. Asignar instructor a curso");
            System.out.println("0. Volver");
            String opcion = consola.leerTexto("Opción: ");
            try {
                switch (opcion) {
                    case "1" -> listarPeriodos();
                    case "2" -> registrarPeriodo();
                    case "3" -> renombrarPeriodo();
                    case "4" -> mostrar("Período activado", controller.activarPeriodo(pedirPeriodo()));
                    case "5" -> finalizarPeriodo();
                    case "6" -> imprimirCursos(cursoRepository.listarTodos());
                    case "7" -> imprimirCursos(controller.listarCursosDelPeriodo(pedirPeriodo()));
                    case "8" -> registrarCurso();
                    case "9" -> cambiarEstadoCurso();
                    case "10" -> asignarInstructor();
                    case "0" -> volver = true;
                    default -> System.out.println("Opción no válida.");
                }
            } catch (RuntimeException e) {
                // Reglas del dominio: transiciones invalidas, ids inexistentes, nombres repetidos, etc.
                System.err.println("[Error] " + e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------- periodos

    private void listarPeriodos() {
        System.out.println("\n--- Períodos ---");
        List<PeriodoAcademico> lista = controller.listarPeriodos();
        if (lista.isEmpty()) {
            System.out.println("  (no hay períodos)");
        }
        for (PeriodoAcademico p : lista) {
            System.out.println("  [" + p.getIdPeriodo() + "] " + p.getNombrePeriodo() + " | "
                    + p.getFechaInicio() + " a " + p.getFechaFin() + " | " + p.getEstado());
        }
    }

    private int pedirPeriodo() {
        listarPeriodos();
        return consola.leerEntero("Id del período: ");
    }

    private void registrarPeriodo() {
        String nombre = consola.leerTextoObligatorio("Nombre del período (ej. Ciclo I - 2027): ");
        LocalDate inicio = consola.leerFecha("Fecha de inicio (AAAA-MM-DD): ");
        LocalDate fin = consola.leerFecha("Fecha de fin (AAAA-MM-DD): ");
        mostrar("Período registrado", controller.registrarPeriodo(new PeriodoAcademico(nombre, inicio, fin)));
    }

    private void renombrarPeriodo() {
        int id = pedirPeriodo();
        String nombre = consola.leerTextoObligatorio("Nuevo nombre: ");
        mostrar("Período renombrado", controller.modificarPeriodo(id, nombre));
    }

    private void finalizarPeriodo() {
        int id = pedirPeriodo();
        if (consola.confirmar("Un período finalizado ya no admite cambios ni matrículas. ¿Continuar?")) {
            mostrar("Período finalizado", controller.finalizarPeriodo(id));
        }
    }

    // ---------------------------------------------------------------- cursos

    private void imprimirCursos(List<Curso> cursos) {
        System.out.println("\n--- Cursos ---");
        if (cursos.isEmpty()) {
            System.out.println("  (no hay cursos)");
        }
        for (Curso c : cursos) {
            String instructor = "sin instructor";
            if (c.getIdInstructor() != null) {
                Usuario u = usuarioRepository.buscarPorId(c.getIdInstructor());
                instructor = u != null ? u.getNombreUsuario() + " " + u.getApellidoUsuario() : "id " + c.getIdInstructor();
            }
            System.out.println("  [" + c.getIdCurso() + "] " + c.getCodigoCurso() + " - " + c.getNombreCurso()
                    + " | período " + c.getIdPeriodo() + " | " + c.getEstado() + " | " + instructor);
        }
    }

    private int pedirCurso() {
        imprimirCursos(cursoRepository.listarTodos());
        return consola.leerEntero("Id del curso: ");
    }

    private void registrarCurso() {
        String codigo = consola.leerTextoObligatorio("Código (ej. PROG-III): ");
        String nombre = consola.leerTextoObligatorio("Nombre: ");
        String descripcion = consola.leerTexto("Descripción (opcional): ");
        int idPeriodo = pedirPeriodo();

        Curso curso = controller.registrarCurso(new Curso(codigo, nombre, descripcion, idPeriodo));
        System.out.println("Curso registrado con id " + curso.getIdCurso() + " (nace " + curso.getEstado() + ").");
        if (consola.confirmar("¿Activarlo ahora?")) {
            controller.activarCurso(curso.getIdCurso());
            System.out.println("Curso activado.");
        }
    }

    private void cambiarEstadoCurso() {
        int id = pedirCurso();
        Curso curso = cursoRepository.buscarPorId(id).orElse(null);
        if (curso == null) {
            System.out.println("No existe el curso con id " + id + ".");
            return;
        }
        Curso actualizado = switch (curso.getEstado()) {
            case ACTIVO -> controller.desactivarCurso(id);
            case INACTIVO -> controller.activarCurso(id);
        };
        System.out.println("Curso " + id + " ahora está " + actualizado.getEstado() + ".");
    }

    private void asignarInstructor() {
        int idCurso = pedirCurso();
        System.out.println("\n--- Profesores ---");
        List<Usuario> usuarios = usuarioRepository.listarTodos();
        MenuUsuarios.listarPorRol(usuarios, "Profesor");
        int idInstructor = consola.leerEntero("Id del profesor: ");

        Usuario profesor = usuarioRepository.buscarPorId(idInstructor);
        if (profesor == null || profesor.getRol() == null
                || !"Profesor".equalsIgnoreCase(profesor.getRol().getNombreRol())) {
            System.out.println("El usuario " + idInstructor + " no es un profesor.");
            return;
        }
        mostrar("Instructor asignado", controller.asignarInstructor(idCurso, idInstructor));
    }

    private void mostrar(String titulo, Object entidad) {
        System.out.println(titulo + ": " + entidad);
    }
}
