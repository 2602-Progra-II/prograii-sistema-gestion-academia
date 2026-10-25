package sv.gestionacademica.repository;

import sv.gestionacademica.entity.Identificable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class InMemoryRepository<T extends Identificable<ID>, ID> {

    protected final List<T> items = new ArrayList<>();

    public T save(T entidad) {
        if (entidad == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }
        if (entidad.getId() != null) {
            deleteById(entidad.getId());
        }
        items.add(entidad);
        return entidad;
    }

    public Optional<T> findById(ID id) {
        if (id == null) {
            return Optional.empty();
        }
        return items.stream()
                .filter(item -> Objects.equals(id, item.getId()))
                .findFirst();
    }

    public List<T> findAll() {
        return new ArrayList<>(items);
    }

    public boolean deleteById(ID id) {
        if (id == null) {
            return false;
        }
        return items.removeIf(item -> item.getId() != null && Objects.equals(id, item.getId()));
    }

    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }
}
