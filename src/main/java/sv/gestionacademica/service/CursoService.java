
package sv.gestionacademica.service;

import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.entity.PeriodoAcademico;
import sv.gestionacademica.enums.EstadoPeriodo;
import sv.gestionacademica.repository.CursoRepository;
import sv.gestionacademica.repository.PeriodoAcademicoRepository;

import java.util.List;
import java.util.NoSuchElementException;

public class CursoService {

    private final CursoRepository cursoRepository;
    private final PeriodoAcademicoRepository periodoAcademicoRepository;

    public CursoService(CursoRepository cursoRepository,
                         PeriodoAcademicoRepository periodoAcademicoRepository) {
        this.cursoRepository = cursoRepository;
        this.periodoAcademicoRepository = periodoAcademicoRepository;
    }

    public Curso registrarCurso(Curso curso) {
        periodoAcademicoRepository.buscarPorId(curso.getIdPeriodo())
                .orElseThrow(() -> new NoSuchElementException(
                        "No existe un periodo académico con id " + curso.getIdPeriodo()));

        if (curso.getCodigoCurso() == null || curso.getCodigoCurso().isBlank()) {
            throw new IllegalArgumentException("El código del curso es obligatorio.");
        }
        if (cursoRepository.buscarPorCodigo(curso.getCodigoCurso()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un curso con el código " + curso.getCodigoCurso());
        }
        return cursoRepository.guardar(curso);
    }

    public Curso asignarInstructor(int idCurso, int idInstructor) {
        Curso curso = obtenerOFallar(idCurso);

        PeriodoAcademico periodo = periodoAcademicoRepository.buscarPorId(curso.getIdPeriodo())
                .orElseThrow(() -> new NoSuchElementException(
                        "El curso hace referencia a un periodo que ya no existe."));
        if (periodo.getEstado() == EstadoPeriodo.FINALIZADO) {
            throw new IllegalStateException("No se puede asignar instructor a un curso de un periodo finalizado.");
        }

        curso.asignarInstructor(idInstructor);
        return cursoRepository.actualizar(curso);
    }

    public Curso activarCurso(int idCurso) {
        Curso curso = obtenerOFallar(idCurso);
        curso.activar();
        return cursoRepository.actualizar(curso);
    }

    public Curso desactivarCurso(int idCurso) {
        Curso curso = obtenerOFallar(idCurso);
        curso.desactivar();
        return cursoRepository.actualizar(curso);
    }

    public Curso consultarCurso(int idCurso) {
        return obtenerOFallar(idCurso);
    }

    public List<Curso> listarPorPeriodo(int idPeriodo) {
        return cursoRepository.listarPorPeriodo(idPeriodo);
    }

    private Curso obtenerOFallar(int idCurso) {
        return cursoRepository.buscarPorId(idCurso)
                .orElseThrow(() -> new NoSuchElementException("No existe un curso con id " + idCurso));
    }
}
