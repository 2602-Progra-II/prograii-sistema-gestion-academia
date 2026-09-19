
package sv.gestionacademica.repository;

import sv.gestionacademica.entity.PeriodoAcademico;

import java.util.List;
import java.util.Optional;

public interface PeriodoAcademicoRepository {

    PeriodoAcademico guardar(PeriodoAcademico periodo);

    Optional<PeriodoAcademico> buscarPorId(int idPeriodo);

    Optional<PeriodoAcademico> buscarPorNombre(String nombrePeriodo);

    List<PeriodoAcademico> listarTodos();

    List<PeriodoAcademico> listarVigentes();

    PeriodoAcademico actualizar(PeriodoAcademico periodo);

    void eliminar(int idPeriodo);
}
