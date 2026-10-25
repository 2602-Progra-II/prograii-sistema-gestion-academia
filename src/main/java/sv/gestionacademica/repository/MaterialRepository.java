package sv.gestionacademica.repository;

import sv.gestionacademica.entity.MaterialDidactico;
import java.util.List;
import java.util.Optional;

public interface MaterialRepository {
    MaterialDidactico save(MaterialDidactico material);
    Optional<MaterialDidactico> findById(String id);
    List<MaterialDidactico> findAll();
    void update(MaterialDidactico material);
    boolean deleteById(String id);
    boolean existsById(String id);
}