package sv.gestionacademica.repository;

import sv.gestionacademica.entity.Matricula;
import java.util.List;
import java.util.Optional;

public interface MatriculaRepository {
    Matricula save(Matricula matricula);
    Optional<Matricula> findById(int idMatricula);
    List<Matricula> findAll();
    List<Matricula> findByEstudiante(String idEstudiante);
    List<Matricula> findByCurso(int idCurso);
    void update(Matricula matricula);
    boolean deleteById(int idMatricula);
    boolean existsById(int idMatricula);
}