package sv.gestionacademica.repository;

import sv.gestionacademica.entity.ActividadEvaluativa;
import java.util.List;
import java.util.Optional;

public interface ActividadRepository {
    ActividadEvaluativa save(ActividadEvaluativa actividad);
    Optional<ActividadEvaluativa> findById(String id);
    List<ActividadEvaluativa> findAll();
    void update(ActividadEvaluativa actividad);
    boolean deleteById(String id);
    boolean existsById(String id);
}   