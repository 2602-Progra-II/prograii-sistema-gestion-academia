package sv.gestionacademica.repository;

import sv.gestionacademica.entity.MaterialDidactico;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaterialRepository {
    private final List<MaterialDidactico> materiales = new ArrayList<>();

    public MaterialDidactico save(MaterialDidactico material) {
        if (material == null) {
            throw new IllegalArgumentException("El material no puede ser nulo");
        }
        deleteById(material.getId());
        materiales.add(material);
        return material;
    }

    public Optional<MaterialDidactico> findById(String id) {
        if (id == null) return Optional.empty();
        return materiales.stream()
                .filter(m -> id.equals(m.getId()))
                .findFirst();
    }

    public List<MaterialDidactico> findAll() {
        return new ArrayList<>(materiales);
    }

    public boolean deleteById(String id) {
        return materiales.removeIf(m -> m.getId() != null && m.getId().equals(id));
    }

    public boolean existsById(String id) {
        return findById(id).isPresent();
    }
}