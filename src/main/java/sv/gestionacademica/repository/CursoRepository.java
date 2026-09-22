
package sv.gestionacademica.repository;

import sv.gestionacademica.entity.Curso;

import java.util.List;
import java.util.Optional;

public interface CursoRepository {

    Curso guardar(Curso curso);

    Optional<Curso> buscarPorId(int idCurso);

    Optional<Curso> buscarPorCodigo(String codigoCurso);

    List<Curso> listarTodos();

    List<Curso> listarPorPeriodo(int idPeriodo);

    List<Curso> listarPorInstructor(int idInstructor);

    Curso actualizar(Curso curso);

    void eliminar(int idCurso);
}
