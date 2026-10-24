package sv.gestionacademica.repository;

import sv.gestionacademica.entity.Rol;
import java.util.List;
import java.util.Optional;

public interface RolRepository {
    Rol save(Rol rol);
    Optional<Rol> findById(int idRol);
    List<Rol> findAll();
    void update(Rol rol);
    boolean deleteById(int idRol);
    boolean existsById(int idRol);
}