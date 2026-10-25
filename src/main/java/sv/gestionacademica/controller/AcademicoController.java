package sv.gestionacademica.controller;


import sv.gestionacademica.entity.ActividadEvaluativa;
import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.entity.EntregaEvaluacion;
import sv.gestionacademica.entity.MaterialDidactico;
import sv.gestionacademica.entity.Matricula;
import sv.gestionacademica.entity.PeriodoAcademico;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.enums.EstadoMatricula;
import sv.gestionacademica.enums.TipoActividad;
import sv.gestionacademica.exception.BusinessException;
import sv.gestionacademica.repository.MatriculaRepository;
import sv.gestionacademica.service.ActividadService;
import sv.gestionacademica.service.CursoService;
import sv.gestionacademica.service.EvaluacionService;
import sv.gestionacademica.service.MaterialService;
import sv.gestionacademica.service.PeriodoAcademicoService;

import java.time.LocalDate;
import java.util.List;


public class AcademicoController {

    private final PeriodoAcademicoService periodoAcademicoService;
    private final CursoService cursoService;
    private final MaterialService materialService;
    private final ActividadService actividadService;
    private final EvaluacionService evaluacionService;
    private final MatriculaRepository matriculaRepository;


    /** Solo periodos y cursos (material, actividades y evaluaciones quedan sin conectar). */
    public AcademicoController(PeriodoAcademicoService periodoAcademicoService,
                                CursoService cursoService) {
        this(periodoAcademicoService, cursoService, null, null, null, null);
    }

    public AcademicoController(PeriodoAcademicoService periodoAcademicoService,
                                CursoService cursoService,
                                MaterialService materialService,
                                ActividadService actividadService,
                                EvaluacionService evaluacionService,
                                MatriculaRepository matriculaRepository) {
        this.periodoAcademicoService = periodoAcademicoService;
        this.cursoService = cursoService;
        this.materialService = materialService;
        this.actividadService = actividadService;
        this.evaluacionService = evaluacionService;
        this.matriculaRepository = matriculaRepository;
    }

    // ---------------------------------------------------------------
    // Periodos académicos
    // ---------------------------------------------------------------

    public PeriodoAcademico registrarPeriodo(PeriodoAcademico periodo) {
        return periodoAcademicoService.registrarPeriodo(periodo);
    }

    public PeriodoAcademico modificarPeriodo(int idPeriodo, String nuevoNombre) {
        return periodoAcademicoService.modificarPeriodo(idPeriodo, nuevoNombre);
    }

    public PeriodoAcademico activarPeriodo(int idPeriodo) {
        return periodoAcademicoService.activarPeriodo(idPeriodo);
    }

    public PeriodoAcademico finalizarPeriodo(int idPeriodo) {
        return periodoAcademicoService.finalizarPeriodo(idPeriodo);
    }

    public List<PeriodoAcademico> listarPeriodos() {
        return periodoAcademicoService.listarPeriodos();
    }

    // ---------------------------------------------------------------
    // Cursos
    // ---------------------------------------------------------------

    public Curso registrarCurso(Curso curso) {
        return cursoService.registrarCurso(curso);
    }

    public Curso asignarInstructor(int idCurso, int idInstructor) {
        return cursoService.asignarInstructor(idCurso, idInstructor);
    }

    public List<Curso> listarCursosDelPeriodo(int idPeriodo) {
        return cursoService.listarPorPeriodo(idPeriodo);
    }

    // ---------------------------------------------------------------
    // Material didactico
    // ---------------------------------------------------------------

    /** Publica material en un curso. Solo el instructor del curso (o un Administrador). */
    public MaterialDidactico publicarMaterial(Usuario autor, int idCurso, String titulo,
                                              String descripcion, String urlEnlace) {
        exigirModulo(materialService);
        Curso curso = cursoService.consultarCurso(idCurso);
        exigirInstructorDelCurso(autor, curso);

        MaterialDidactico material = new MaterialDidactico("0", titulo, descripcion, urlEnlace);
        material.setIdCurso(curso.getIdCurso());
        return materialService.registrarMaterial(material);
    }

    public List<MaterialDidactico> listarMaterialDelCurso(int idCurso) {
        exigirModulo(materialService);
        return materialService.obtenerTodosLosMateriales().stream()
                .filter(m -> m.getIdCurso() == idCurso)
                .toList();
    }

    // ---------------------------------------------------------------
    // Actividades evaluativas
    // ---------------------------------------------------------------

    /**
     * Crea una actividad en un curso. Solo el instructor del curso (o un Administrador).
     * Regla: la suma de ponderaciones de las actividades de un curso no puede pasar de 100%.
     */
    public ActividadEvaluativa crearActividad(Usuario autor, int idCurso, TipoActividad tipo, String nombre,
                                              String descripcion, LocalDate fechaLimite, double ponderacion) {
        exigirModulo(actividadService);
        Curso curso = cursoService.consultarCurso(idCurso);
        exigirInstructorDelCurso(autor, curso);

        double asignado = listarActividadesDelCurso(idCurso).stream()
                .mapToDouble(ActividadEvaluativa::getPonderacion).sum();
        if (asignado + ponderacion > 100.0) {
            throw new BusinessException(String.format(
                    "La ponderación excede el 100%% del curso: ya hay %.2f%% asignado, quedan %.2f%%.",
                    asignado, 100.0 - asignado));
        }

        // Cada tipo construye su especializacion (Tarea, Examen o Proyecto).
        ActividadEvaluativa actividad = tipo.crear(0, nombre, descripcion, fechaLimite, ponderacion);
        actividad.setIdCurso(curso.getIdCurso());
        return actividadService.registrarActividad(actividad);
    }

    public List<ActividadEvaluativa> listarActividadesDelCurso(int idCurso) {
        exigirModulo(actividadService);
        return actividadService.obtenerTodasLasActividades().stream()
                .filter(a -> a.getIdCurso() == idCurso)
                .toList();
    }

    public ActividadEvaluativa consultarActividad(int idActividad) {
        exigirModulo(actividadService);
        return actividadService.obtenerActividadPorId(String.valueOf(idActividad));
    }

    // ---------------------------------------------------------------
    // Entregas y calificaciones
    // ---------------------------------------------------------------

    /**
     * El estudiante entrega una actividad.
     * Reglas: debe tener matricula ACTIVA en el curso; despues de la fecha limite solo se acepta
     * si el tipo de actividad lo permite (Tarea, con penalizacion); y, si ya entrego, puede
     * reemplazar su entrega solo mientras no este calificada.
     */
    public EntregaEvaluacion entregarActividad(Usuario estudiante, int idActividad, String rutaArchivo) {
        exigirModulo(evaluacionService);
        ActividadEvaluativa actividad = consultarActividad(idActividad);

        if (!tieneMatriculaActiva(estudiante, actividad.getIdCurso())) {
            throw new BusinessException("No tienes una matrícula activa en el curso de esta actividad.");
        }
        if (actividad.estaVencida() && !actividad.permiteEntregaTardia()) {
            throw new BusinessException("La fecha límite (" + actividad.getFechaEntrega() + ") ya pasó y un "
                    + actividad.getTipo().name().toLowerCase() + " no admite entregas tardías.");
        }

        EntregaEvaluacion entrega = buscarEntregaDelEstudiante(estudiante, idActividad);
        if (entrega != null && entrega.estaCalificada()) {
            throw new BusinessException("Tu entrega ya fue calificada; no se puede reemplazar.");
        }
        if (entrega == null) {
            entrega = new EntregaEvaluacion();
            entrega.setIdActividad(idActividad);
        }
        entrega.setRutaArchivoEntrega(rutaArchivo);
        entrega.setFechaEntrega(LocalDate.now());
        return evaluacionService.entregarActividad(entrega, estudiante);
    }

    /** Califica una entrega (0 a 10). Solo el instructor del curso (o un Administrador). */
    public EntregaEvaluacion calificarActividad(Usuario instructor, int idEntrega, double nota, String observaciones) {
        exigirModulo(evaluacionService);
        EntregaEvaluacion entrega = evaluacionService.buscarEntrega(idEntrega)
                .orElseThrow(() -> new BusinessException("No existe la entrega con id " + idEntrega));
        ActividadEvaluativa actividad = consultarActividad(entrega.getIdActividad());
        exigirInstructorDelCurso(instructor, cursoService.consultarCurso(actividad.getIdCurso()));

        if (observaciones == null || observaciones.isBlank()) {
            observaciones = "Calificado";   // estaCalificada() exige observaciones
        }
        entrega.setCalificacion(nota);
        entrega.setObservaciones(observaciones);
        evaluacionService.calificarActividad(entrega, instructor);
        return entrega;
    }

    public List<EntregaEvaluacion> listarEntregasDeActividad(int idActividad) {
        exigirModulo(evaluacionService);
        return evaluacionService.listarEntregasDeActividad(idActividad);
    }

    public List<EntregaEvaluacion> consultarCalificaciones(Usuario estudiante) {
        exigirModulo(evaluacionService);
        return evaluacionService.consultarCalificaciones(estudiante);
    }

    /** Cursos donde el estudiante tiene matricula ACTIVA (los unicos cuyo contenido puede ver). */
    public List<Curso> cursosDelEstudiante(Usuario estudiante) {
        exigirModulo(matriculaRepository);
        return matriculaRepository.findByEstudiante(String.valueOf(estudiante.getIdUsuario())).stream()
                .filter(m -> m.getEstadoMatricula() == EstadoMatricula.ACTIVA && m.getCurso() != null)
                .map(Matricula::getCurso)
                .toList();
    }

    // ---------------------------------------------------------------
    // Reglas compartidas
    // ---------------------------------------------------------------

    private void exigirInstructorDelCurso(Usuario usuario, Curso curso) {
        if (esRol(usuario, "Administrador")) {
            return;
        }
        if (!esRol(usuario, "Profesor")
                || curso.getIdInstructor() == null
                || curso.getIdInstructor() != usuario.getIdUsuario()) {
            throw new BusinessException("Solo el instructor del curso " + curso.getCodigoCurso()
                    + " puede hacer esta operación.");
        }
    }

    private boolean tieneMatriculaActiva(Usuario estudiante, int idCurso) {
        return cursosDelEstudiante(estudiante).stream().anyMatch(c -> c.getIdCurso() == idCurso);
    }

    private EntregaEvaluacion buscarEntregaDelEstudiante(Usuario estudiante, int idActividad) {
        return evaluacionService.consultarCalificaciones(estudiante).stream()
                .filter(e -> e.getIdActividad() == idActividad)
                .findFirst()
                .orElse(null);
    }

    private static boolean esRol(Usuario u, String rol) {
        return u != null && u.getRol() != null && rol.equalsIgnoreCase(u.getRol().getNombreRol());
    }

    private static void exigirModulo(Object dependencia) {
        if (dependencia == null) {
            throw new IllegalStateException(
                    "AcademicoController se creó sin los servicios de material/actividades/evaluaciones.");
        }
    }

    public Curso activarCurso(int idCurso) {
        return cursoService.activarCurso(idCurso);
    }

    public Curso desactivarCurso(int idCurso) {
        return cursoService.desactivarCurso(idCurso);
    }
}
