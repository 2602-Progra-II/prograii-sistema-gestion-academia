package sv.gestionacademica.controller;


import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.entity.PeriodoAcademico;
import sv.gestionacademica.service.CursoService;
import sv.gestionacademica.service.PeriodoAcademicoService;

import java.util.List;


public class AcademicoController {

    private final PeriodoAcademicoService periodoAcademicoService;
    private final CursoService cursoService;


    public AcademicoController(PeriodoAcademicoService periodoAcademicoService,
                                CursoService cursoService) {
        this.periodoAcademicoService = periodoAcademicoService;
        this.cursoService = cursoService;
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
    // Pendiente: se completa cuando existan los servicios de otras ramas.
    // ---------------------------------------------------------------

    public void matricularEstudiante() {
        throw new UnsupportedOperationException(
                "Pendiente: se implementa cuando se integre MatriculaService (otra rama).");
    }

    public void publicarMaterial() {
        throw new UnsupportedOperationException(
                "Pendiente: se implementa cuando se integre MaterialService (otra rama).");
    }

    public void crearActividad() {
        throw new UnsupportedOperationException(
                "Pendiente: se implementa cuando se integre ActividadService (otra rama).");
    }

    public void entregarActividad() {
        throw new UnsupportedOperationException(
                "Pendiente: se implementa cuando se integre EvaluacionService (otra rama).");
    }

    public void calificarActividad() {
        throw new UnsupportedOperationException(
                "Pendiente: se implementa cuando se integre EvaluacionService (otra rama).");
    }

    public Curso activarCurso(int idCurso) {
        return cursoService.activarCurso(idCurso);
    }

    public Curso desactivarCurso(int idCurso) {
        return cursoService.desactivarCurso(idCurso);
    }
}
