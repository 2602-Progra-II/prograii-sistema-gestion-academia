package sv.gestionacademica.service;

import sv.gestionacademica.entity.ActividadEvaluativa;
import sv.gestionacademica.repository.ActividadRepository;

import java.util.List;
import java.util.Optional;

public class ActividadService {
    private final ActividadRepository actividadRepository;

    public ActividadService() {
        this.actividadRepository = new ActividadRepository();
    }

    public ActividadService(ActividadRepository actividadRepository) {
        this.actividadRepository = actividadRepository;
    }

    public ActividadEvaluativa registrarActividad(ActividadEvaluativa actividad) {
        if (actividad == null) {
            throw new IllegalArgumentException("La actividad evaluativa no puede ser nula");
        }
        if (actividad.getId() == null || actividad.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la actividad es obligatorio");
        }
        if (actividad.getNombre() == null || actividad.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la actividad es obligatorio");
        }
        if (actividad.getPonderacion() <= 0 || actividad.getPonderacion() > 100) {
            throw new IllegalArgumentException("La ponderación debe estar entre 0.01 y 100.0");
        }
        return actividadRepository.save(actividad);
    }

    public List<ActividadEvaluativa> obtenerTodasLasActividades() {
        return actividadRepository.findAll();
    }

    public Optional<ActividadEvaluativa> buscarActividadPorId(String id) {
        return actividadRepository.findById(id);
    }

    public boolean eliminarActividad(String id) {
        return actividadRepository.deleteById(id);
    }
}