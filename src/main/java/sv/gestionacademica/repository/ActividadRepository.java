package sv.gestionacademica.repository;

import sv.gestionacademica.entity.ActividadEvaluativa;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ActividadRepository {
    private final List<ActividadEvaluativa> actividades = new ArrayList<>();

    public ActividadEvaluativa save(ActividadEvaluativa actividad) {
        if (actividad == null) {
            throw new IllegalArgumentException("La actividad evaluativa no puede ser nula");
        }
        deleteById(actividad.getId());
        actividades.add(actividad);
        return actividad;
    }

    public Optional<ActividadEvaluativa> findById(String id) {
        if (id == null) return Optional.empty();
        return actividades.stream()
                .filter(a -> id.equals(a.getId()))
                .findFirst();
    }

    public List<ActividadEvaluativa> findAll() {
        return new ArrayList<>(actividades);
    }

    public boolean deleteById(String id) {
        return actividades.removeIf(a -> a.getId() != null && a.getId().equals(id));
    }

    public boolean existsById(String id) {
        return findById(id).isPresent();
    }
}