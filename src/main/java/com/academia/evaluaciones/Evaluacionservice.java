package com.academia;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class EvaluacionService {

    public static final double CALIFICACION_MINIMA = 0.0;
    public static final double CALIFICACION_MAXIMA = 10.0;

    private final EntregaRepository entregaRepository;

    public EvaluacionService(EntregaRepository entregaRepository) {
        if (entregaRepository == null) {
            throw new IllegalArgumentException("El repositorio de entregas no puede ser nulo");
        }
        this.entregaRepository = entregaRepository;
    }


    public EntregaEvaluacion entregarActividad(ActividadEvaluativa actividad, String idEstudiante, String rutaArchivoEntrega) {
        if (actividad == null) {
            throw new IllegalArgumentException("La actividad no puede ser nula");
        }
        if (idEstudiante == null || idEstudiante.trim().isEmpty()) {
            throw new IllegalArgumentException("El id del estudiante no puede estar vacío");
        }
        if (actividad.estaVencida()) {
            throw new IllegalStateException(
                    "No es posible entregar: la actividad venció el " + actividad.getFechaLimite());
        }
        boolean yaEntrego = entregaRepository.listarPorActividad(actividad.getIdActividad()).stream()
                .anyMatch(entrega -> entrega.getIdEstudiante().equals(idEstudiante));
        if (yaEntrego) {
            throw new IllegalStateException("El estudiante ya registró una entrega para esta actividad");
        }
        EntregaEvaluacion entrega = new EntregaEvaluacion(
                actividad.getIdActividad(), idEstudiante, rutaArchivoEntrega, LocalDate.now());
        return entregaRepository.guardar(entrega);
    }


    public EntregaEvaluacion calificarActividad(int idEntrega, double calificacion, String observaciones, String idInstructor) {
        if (idInstructor == null || idInstructor.trim().isEmpty()) {
            throw new IllegalArgumentException("El id del instructor no puede estar vacío");
        }
        EntregaEvaluacion entrega = entregaRepository.buscarPorId(idEntrega);
        if (entrega == null) {
            throw new IllegalArgumentException("No existe una entrega con id " + idEntrega);
        }
        entrega.setCalificacion(calificacion); // valida el rango internamente
        entrega.setObservaciones(observaciones);
        return entregaRepository.actualizar(entrega);
    }


    public List<EntregaEvaluacion> consultarCalificaciones(String idEstudiante) {
        if (idEstudiante == null || idEstudiante.trim().isEmpty()) {
            throw new IllegalArgumentException("El id del estudiante no puede estar vacío");
        }
        return entregaRepository.listarPorEstudiante(idEstudiante);
    }


    public List<EntregaEvaluacion> consultarCalificacionesPorCurso(String idEstudiante, List<ActividadEvaluativa> actividadesDelCurso) {
        if (actividadesDelCurso == null) {
            throw new IllegalArgumentException("La lista de actividades del curso no puede ser nula");
        }
        return consultarCalificaciones(idEstudiante).stream()
                .filter(entrega -> actividadesDelCurso.stream()
                        .anyMatch(actividad -> actividad.getIdActividad() == entrega.getIdActividad()))
                .collect(Collectors.toList());
    }

    public List<EntregaEvaluacion> consultarEntregasPorActividad(int idActividad) {
        return entregaRepository.listarPorActividad(idActividad);
    }
}