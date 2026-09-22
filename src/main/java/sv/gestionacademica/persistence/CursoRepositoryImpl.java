package sv.gestionacademica.persistence;

import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.repository.CursoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class CursoRepositoryImpl implements CursoRepository {

    private final List<Curso> cursos = new ArrayList<>();
    private int secuenciaId = 1;

    @Override
    public Curso guardar(Curso curso) {
        curso.setIdCurso(secuenciaId++);
        cursos.add(curso);
        return curso;
    }

    @Override
    public Optional<Curso> buscarPorId(int idCurso) {
        return cursos.stream()
                .filter(c -> c.getIdCurso() == idCurso)
                .findFirst();
    }

    @Override
    public Optional<Curso> buscarPorCodigo(String codigoCurso) {
        return cursos.stream()
                .filter(c -> c.getCodigoCurso().equalsIgnoreCase(codigoCurso))
                .findFirst();
    }

    @Override
    public List<Curso> listarTodos() {
        return new ArrayList<>(cursos);
    }

    @Override
    public List<Curso> listarPorPeriodo(int idPeriodo) {
        return cursos.stream()
                .filter(c -> c.getIdPeriodo() == idPeriodo)
                .toList();
    }

    @Override
    public List<Curso> listarPorInstructor(int idInstructor) {
        return cursos.stream()
                .filter(c -> c.getIdInstructor() != null && c.getIdInstructor() == idInstructor)
                .toList();
    }

    @Override
    public Curso actualizar(Curso curso) {
        return curso; // misma razón que en PeriodoAcademicoRepositoryImpl
    }

    @Override
    public void eliminar(int idCurso) {
        cursos.removeIf(c -> c.getIdCurso() == idCurso);
    }
}
