package sv.gestionacademica.service;

import sv.gestionacademica.entity.MaterialDidactico;
import sv.gestionacademica.repository.MaterialRepository;

import java.util.List;
import java.util.Optional;

public class MaterialService {
    private final MaterialRepository materialRepository;

    public MaterialService() {
        this.materialRepository = new MaterialRepository();
    }

    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    public MaterialDidactico registrarMaterial(MaterialDidactico material) {
        if (material == null) {
            throw new IllegalArgumentException("El material didáctico no puede ser nulo");
        }
        if (material.getId() == null || material.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del material es obligatorio");
        }
        if (material.getTitulo() == null || material.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("El título del material es obligatorio");
        }
        return materialRepository.save(material);
    }

    public List<MaterialDidactico> obtenerTodosLosMateriales() {
        return materialRepository.findAll();
    }

    public Optional<MaterialDidactico> buscarMaterialPorId(String id) {
        return materialRepository.findById(id);
    }

    public boolean eliminarMaterial(String id) {
        return materialRepository.deleteById(id);
    }
}