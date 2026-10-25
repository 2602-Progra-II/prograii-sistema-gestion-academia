package sv.gestionacademica.repository;

import sv.gestionacademica.entity.Matricula;

import java.sql.Connection;
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

    // Variantes que participan en una transaccion: usan la Connection recibida
    // y NO la cierran, commit/rollback los controla quien la abrio.
    Matricula save(Connection con, Matricula matricula);
    Optional<Matricula> findById(Connection con, int idMatricula);
    List<Matricula> findByEstudiante(Connection con, String idEstudiante);
    void update(Connection con, Matricula matricula);
}
