package sv.gestionacademica.menu;

import sv.gestionacademica.controller.AcademicoController;
import sv.gestionacademica.entity.ActividadEvaluativa;
import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.entity.EntregaEvaluacion;
import sv.gestionacademica.entity.MaterialDidactico;
import sv.gestionacademica.entity.Sesion;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.enums.TipoActividad;
import sv.gestionacademica.repository.CursoRepository;
import sv.gestionacademica.repository.UsuarioRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Material didactico, actividades evaluativas, entregas y calificaciones.
 * Profesor/Administrador: publica material, crea actividades y califica.
 * Estudiante: ve material y actividades de sus cursos, entrega y consulta sus notas.
 */
public class MenuAcademico implements ModuloMenu {

    private final Consola consola;
    private final AcademicoController controller;
    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;

    public MenuAcademico(Consola consola, AcademicoController controller,
                         CursoRepository cursoRepository, UsuarioRepository usuarioRepository) {
        this.consola = consola;
        this.controller = controller;
        this.cursoRepository = cursoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void ejecutar(Sesion sesion) {
        Usuario u = sesion.getUsuario();
        if ("Estudiante".equalsIgnoreCase(u.getRol().getNombreRol())) {
            menuEstudiante(u);
        } else {
            menuDocente(u);
        }
    }

    // ================================================================ Profesor / Administrador

    private void menuDocente(Usuario docente) {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n===== MATERIAL, ACTIVIDADES Y CALIFICACIONES =====");
            System.out.println("1. Ver material de un curso");
            System.out.println("2. Publicar material");
            System.out.println("3. Ver actividades de un curso");
            System.out.println("4. Crear actividad");
            System.out.println("5. Ver entregas de una actividad");
            System.out.println("6. Calificar entrega");
            System.out.println("0. Volver");
            String opcion = consola.leerTexto("Opción: ");
            try {
                switch (opcion) {
                    case "1" -> imprimirMaterial(controller.listarMaterialDelCurso(elegirCurso(cursosDocente(docente))));
                    case "2" -> publicarMaterial(docente);
                    case "3" -> imprimirActividades(controller.listarActividadesDelCurso(elegirCurso(cursosDocente(docente))));
                    case "4" -> crearActividad(docente);
                    case "5" -> verEntregas(docente);
                    case "6" -> calificar(docente);
                    case "0" -> volver = true;
                    default -> System.out.println("Opción no válida.");
                }
            } catch (RuntimeException e) {
                System.err.println("[Error] " + e.getMessage());
            }
        }
    }

    /** El profesor solo ve sus cursos; el administrador ve todos. */
    private List<Curso> cursosDocente(Usuario docente) {
        if ("Administrador".equalsIgnoreCase(docente.getRol().getNombreRol())) {
            return cursoRepository.listarTodos();
        }
        return cursoRepository.listarPorInstructor(docente.getIdUsuario());
    }

    private void publicarMaterial(Usuario docente) {
        int idCurso = elegirCurso(cursosDocente(docente));
        String titulo = consola.leerTextoObligatorio("Título: ");
        String descripcion = consola.leerTexto("Descripción (opcional): ");
        String url = consola.leerTextoObligatorio("Enlace o ruta del archivo: ");
        MaterialDidactico m = controller.publicarMaterial(docente, idCurso, titulo, descripcion, url);
        System.out.println("Material publicado con id " + m.getId() + ".");
    }

    private void crearActividad(Usuario docente) {
        int idCurso = elegirCurso(cursosDocente(docente));
        imprimirActividades(controller.listarActividadesDelCurso(idCurso));
        TipoActividad tipo = elegirTipo();
        String nombre = consola.leerTextoObligatorio("Nombre de la actividad: ");
        String descripcion = consola.leerTexto("Descripción (opcional): ");
        LocalDate limite = consola.leerFecha("Fecha límite (AAAA-MM-DD): ");
        double ponderacion = leerDecimal("Ponderación en % (ej. 20): ");
        ActividadEvaluativa a = controller.crearActividad(docente, idCurso, tipo, nombre, descripcion, limite, ponderacion);
        System.out.println(a.getTipo() + " creado con id " + a.getId() + ".");
    }

    private void verEntregas(Usuario docente) {
        int idCurso = elegirCurso(cursosDocente(docente));
        List<ActividadEvaluativa> actividades = controller.listarActividadesDelCurso(idCurso);
        imprimirActividades(actividades);
        int idActividad = consola.leerEntero("Id de la actividad: ");
        if (actividades.stream().noneMatch(a -> a.getId().equals(String.valueOf(idActividad)))) {
            throw new IllegalArgumentException("Esa actividad no pertenece al curso elegido.");
        }
        imprimirEntregas(controller.listarEntregasDeActividad(idActividad));
    }

    private void calificar(Usuario docente) {
        verEntregas(docente);
        int idEntrega = consola.leerEntero("Id de la entrega a calificar: ");
        double nota = leerDecimal("Nota (0 a 10): ");
        String obs = consola.leerTexto("Observaciones: ");
        EntregaEvaluacion e = controller.calificarActividad(docente, idEntrega, nota, obs);
        System.out.println("Entrega " + e.getIdEntrega() + " calificada con " + e.getCalificacion() + ".");
    }

    // ================================================================ Estudiante

    private void menuEstudiante(Usuario estudiante) {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n===== MATERIAL, ENTREGAS Y CALIFICACIONES =====");
            System.out.println("1. Ver material de mis cursos");
            System.out.println("2. Ver actividades de mis cursos");
            System.out.println("3. Entregar actividad");
            System.out.println("4. Mis calificaciones");
            System.out.println("0. Volver");
            String opcion = consola.leerTexto("Opción: ");
            try {
                switch (opcion) {
                    case "1" -> imprimirMaterial(controller.listarMaterialDelCurso(elegirCurso(controller.cursosDelEstudiante(estudiante))));
                    case "2" -> imprimirActividades(controller.listarActividadesDelCurso(elegirCurso(controller.cursosDelEstudiante(estudiante))));
                    case "3" -> entregar(estudiante);
                    case "4" -> misCalificaciones(estudiante);
                    case "0" -> volver = true;
                    default -> System.out.println("Opción no válida.");
                }
            } catch (RuntimeException e) {
                System.err.println("[Error] " + e.getMessage());
            }
        }
    }

    private void entregar(Usuario estudiante) {
        int idCurso = elegirCurso(controller.cursosDelEstudiante(estudiante));
        imprimirActividades(controller.listarActividadesDelCurso(idCurso));
        int idActividad = consola.leerEntero("Id de la actividad: ");
        ActividadEvaluativa actividad = controller.consultarActividad(idActividad);
        if (actividad.estaVencida() && actividad.permiteEntregaTardia()
                && !consola.confirmar("La fecha límite ya pasó: la nota tendrá penalización. ¿Entregar de todos modos?")) {
            return;
        }
        String ruta = consola.leerTextoObligatorio("Ruta o enlace de tu archivo: ");
        EntregaEvaluacion e = controller.entregarActividad(estudiante, idActividad, ruta);
        System.out.println("Entrega registrada (id " + e.getIdEntrega() + ", " + e.getFechaEntrega() + ").");
    }

    private void misCalificaciones(Usuario estudiante) {
        System.out.println("\n--- Mis calificaciones ---");
        List<EntregaEvaluacion> entregas = controller.consultarCalificaciones(estudiante);
        if (entregas.isEmpty()) {
            System.out.println("  (no tienes entregas)");
        }
        for (EntregaEvaluacion e : entregas) {
            ActividadEvaluativa a = controller.consultarActividad(e.getIdActividad());
            if (e.estaCalificada()) {
                // Polimorfismo: cada tipo aplica su regla (penalizacion, bonificacion o nota tal cual).
                double notaFinal = a.calcularNotaFinal(e.getCalificacion(), e.getFechaEntrega());
                System.out.printf("  [%s] %s (%.0f%%): nota %.2f -> final %.2f -> aporta %.2f puntos | %s%n",
                        a.getTipo(), a.getNombre(), a.getPonderacion(), e.getCalificacion(), notaFinal,
                        a.calcularAporte(notaFinal), e.getObservaciones());
            } else {
                System.out.printf("  %s (%.0f%%): entregada el %s, pendiente de calificar%n",
                        a.getNombre(), a.getPonderacion(), e.getFechaEntrega());
            }
        }
    }

    // ================================================================ auxiliares

    private int elegirCurso(List<Curso> cursos) {
        if (cursos.isEmpty()) {
            throw new IllegalStateException("No tienes cursos disponibles.");
        }
        System.out.println("\n--- Cursos ---");
        for (Curso c : cursos) {
            System.out.println("  [" + c.getIdCurso() + "] " + c.getCodigoCurso() + " - " + c.getNombreCurso());
        }
        while (true) {
            int id = consola.leerEntero("Id del curso: ");
            if (cursos.stream().anyMatch(c -> c.getIdCurso() == id)) {
                return id;
            }
            System.out.println("Ese curso no está en tu lista.");
        }
    }

    private void imprimirMaterial(List<MaterialDidactico> lista) {
        System.out.println("\n--- Material ---");
        if (lista.isEmpty()) {
            System.out.println("  (sin material)");
        }
        for (MaterialDidactico m : lista) {
            System.out.println("  [" + m.getId() + "] " + m.getTitulo() + " -> " + m.getUrlEnlace()
                    + (m.getDescripcion() == null || m.getDescripcion().isBlank() ? "" : " | " + m.getDescripcion()));
        }
    }

    private void imprimirActividades(List<ActividadEvaluativa> lista) {
        System.out.println("\n--- Actividades ---");
        if (lista.isEmpty()) {
            System.out.println("  (sin actividades)");
        }
        double total = 0;
        for (ActividadEvaluativa a : lista) {
            System.out.printf("  [%s] %s %s | %.2f%% | límite %s%s%n", a.getId(), a.getTipo(), a.getNombre(),
                    a.getPonderacion(), a.getFechaEntrega(), a.estaVencida() ? " (vencida)" : "");
            total += a.getPonderacion();
        }
        if (!lista.isEmpty()) {
            System.out.printf("  Ponderación asignada: %.2f%% de 100%%%n", total);
        }
    }

    private void imprimirEntregas(List<EntregaEvaluacion> lista) {
        System.out.println("\n--- Entregas ---");
        if (lista.isEmpty()) {
            System.out.println("  (sin entregas)");
        }
        for (EntregaEvaluacion e : lista) {
            Usuario est = usuarioRepository.buscarPorId(Integer.parseInt(e.getIdEstudiante()));
            String nombre = est != null ? est.getNombreUsuario() + " " + est.getApellidoUsuario() : "estudiante " + e.getIdEstudiante();
            String estado = e.estaCalificada() ? "nota " + e.getCalificacion() : "pendiente";
            System.out.println("  [" + e.getIdEntrega() + "] " + nombre + " | " + e.getFechaEntrega()
                    + " | " + e.getRutaArchivoEntrega() + " | " + estado);
        }
    }

    private TipoActividad elegirTipo() {
        System.out.println("Tipo: 1. Tarea (acepta entregas tardías con -20%)  "
                + "2. Examen (sin entregas tardías)  3. Proyecto (+5% si se entrega a tiempo)");
        while (true) {
            switch (consola.leerTexto("Tipo: ")) {
                case "1": return TipoActividad.TAREA;
                case "2": return TipoActividad.EXAMEN;
                case "3": return TipoActividad.PROYECTO;
                default: System.out.println("Elige 1, 2 o 3.");
            }
        }
    }

    private double leerDecimal(String mensaje) {
        while (true) {
            try {
                return Double.parseDouble(consola.leerTexto(mensaje).replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un número válido.");
            }
        }
    }
}
