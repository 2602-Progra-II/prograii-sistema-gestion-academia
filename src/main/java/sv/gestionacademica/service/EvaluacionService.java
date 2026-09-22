package sv.gestionacademica.service;

import sv.gestionacademica.entity.EntregaEvaluacion;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.repository.EntregaRepository;

import java.time.LocalDate;
import java.util.List;

public class EvaluacionService {

    private final EntregaRepository entregaRepository;

    public EvaluacionService(EntregaRepository entregaRepository) {
        this.entregaRepository = entregaRepository;
    }

    public EntregaEvaluacion entregarActividad(EntregaEvaluacion entrega, Usuario estudiante) {
        if (entrega == null || estudiante == null) {
            throw new IllegalArgumentException("La entrega y el estudiante no pueden ser nulos.");
        }

        entrega.setIdEstudiante(String.valueOf(estudiante.getIdUsuario()));
        if (entrega.getFechaEntrega() == null) {
            entrega.setFechaEntrega(LocalDate.now());
        }

        return entregaRepository.guardar(entrega);
    }

    public void calificarActividad(EntregaEvaluacion entrega, Usuario instructor) {
        if (entrega == null || instructor == null) {
            throw new IllegalArgumentException("La entrega y el instructor no pueden ser nulos.");
        }

        if (entrega.getCalificacion() < 0.0 || entrega.getCalificacion() > 10.0) {
            throw new IllegalArgumentException("La calificación debe estar entre 0.0 y 10.0.");
        }

        entregaRepository.guardar(entrega);
    }

    public List<EntregaEvaluacion> consultarCalificaciones(Usuario estudiante) {
        if (estudiante == null) {
            throw new IllegalArgumentException("El estudiante no puede ser nulo.");
        }

        return entregaRepository.listarPorEstudiante(String.valueOf(estudiante.getIdUsuario()));
    }
}
