package sv.gestionacademica.service;

import java.util.List;
import java.util.Optional;
import sv.gestionacademica.entity.ActividadEvaluativa;
import sv.gestionacademica.exception.ActividadInvalidaException;
import sv.gestionacademica.exception.ActividadNoEncontradaException;
import sv.gestionacademica.repository.ActividadRepository;
import sv.gestionacademica.repository.ActividadRepositoryImpl;

public class ActividadService {
    private final ActividadRepository actividadRepository;

    public ActividadService() {
        this.actividadRepository = new ActividadRepositoryImpl();
    }

    public ActividadService(ActividadRepository actividadRepository) {
        this.actividadRepository = actividadRepository;
    }

    public ActividadEvaluativa registrarActividad(ActividadEvaluativa actividad) {
        if (actividad == null) {
            throw new ActividadInvalidaException("La actividad evaluativa no puede ser nula");
        }
        if (actividad.getId() == null || actividad.getId().trim().isEmpty()) {
            throw new ActividadInvalidaException("El ID de la actividad es obligatorio");
        }
        if (actividad.getNombre() == null || actividad.getNombre().trim().isEmpty()) {
            throw new ActividadInvalidaException("El nombre de la actividad es obligatorio");
        }
        if (actividad.getPonderacion() <= 0 || actividad.getPonderacion() > 100) {
            throw new ActividadInvalidaException("La ponderación debe estar entre 0.01 y 100.0");
        }
        return actividadRepository.save(actividad);
    }

    public List<ActividadEvaluativa> obtenerTodasLasActividades() {
        return actividadRepository.findAll();
    }

    public Optional<ActividadEvaluativa> buscarActividadPorId(String id) {
        return actividadRepository.findById(id);
    }

    public ActividadEvaluativa obtenerActividadPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ActividadInvalidaException("El ID de la actividad no puede ser nulo o vacío");
        }
        return actividadRepository.findById(id)
                .orElseThrow(() -> new ActividadNoEncontradaException("Actividad evaluativa no encontrada con ID: " + id));
    }

    public boolean eliminarActividad(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ActividadInvalidaException("El ID de la actividad no puede ser nulo o vacío");
        }
        if (!actividadRepository.existsById(id)) {
            throw new ActividadNoEncontradaException("No se encontró la actividad evaluativa con ID: " + id);
        }
        return actividadRepository.deleteById(id);
    }
}
