package sv.gestionacademica.service;

import java.util.List;
import java.util.Optional;
import sv.gestionacademica.entity.MaterialDidactico;
import sv.gestionacademica.exception.MaterialInvalidoException;
import sv.gestionacademica.exception.MaterialNoEncontradoException;
import sv.gestionacademica.repository.MaterialRepository;
import sv.gestionacademica.repository.MaterialRepositoryImpl;

public class MaterialService {
    private final MaterialRepository materialRepository;

    public MaterialService() {
        this.materialRepository = new MaterialRepositoryImpl();
    }

    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    public MaterialDidactico registrarMaterial(MaterialDidactico material) {
        if (material == null) {
            throw new MaterialInvalidoException("El material didáctico no puede ser nulo");
        }
        if (material.getId() == null || material.getId().trim().isEmpty()) {
            throw new MaterialInvalidoException("El ID del material es obligatorio");
        }
        if (material.getTitulo() == null || material.getTitulo().trim().isEmpty()) {
            throw new MaterialInvalidoException("El título del material es obligatorio");
        }
        return materialRepository.save(material);
    }

    public List<MaterialDidactico> obtenerTodosLosMateriales() {
        return materialRepository.findAll();
    }

    public Optional<MaterialDidactico> buscarMaterialPorId(String id) {
        return materialRepository.findById(id);
    }

    public MaterialDidactico obtenerMaterialPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new MaterialInvalidoException("El ID del material no puede ser nulo o vacío");
        }
        return materialRepository.findById(id)
                .orElseThrow(() -> new MaterialNoEncontradoException("Material didáctico no encontrado con ID: " + id));
    }

    public boolean eliminarMaterial(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new MaterialInvalidoException("El ID del material no puede ser nulo o vacío");
        }
        if (!materialRepository.existsById(id)) {
            throw new MaterialNoEncontradoException("No se encontró el material didáctico con ID: " + id);
        }
        return materialRepository.deleteById(id);
    }
}
