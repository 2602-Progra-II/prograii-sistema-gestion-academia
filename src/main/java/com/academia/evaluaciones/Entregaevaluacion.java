package com.academia;

import java.time.LocalDate;

public class EntregaEvaluacion {

    private int idEntrega;
    private final int idActividad;
    private final String idEstudiante;
    private final String rutaArchivoEntrega;
    private final LocalDate fechaEntrega;
    private Double calificacion;
    private String observaciones;

    public EntregaEvaluacion(int idActividad, String idEstudiante, String rutaArchivoEntrega, LocalDate fechaEntrega) {
        if (idActividad <= 0) {
            throw new IllegalArgumentException("El id de la actividad debe ser válido");
        }
        if (idEstudiante == null || idEstudiante.trim().isEmpty()) {
            throw new IllegalArgumentException("El id del estudiante no puede estar vacío");
        }
        if (rutaArchivoEntrega == null || rutaArchivoEntrega.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del archivo de entrega no puede estar vacía");
        }
        if (fechaEntrega == null) {
            throw new IllegalArgumentException("La fecha de entrega no puede ser nula");
        }
        this.idActividad = idActividad;
        this.idEstudiante = idEstudiante;
        this.rutaArchivoEntrega = rutaArchivoEntrega;
        this.fechaEntrega = fechaEntrega;
    }


    public boolean estaCalificada() {
        return calificacion != null;
    }

    public int getIdEntrega() {
        return idEntrega;
    }


    void setIdEntrega(int idEntrega) {
        this.idEntrega = idEntrega;
    }

    public int getIdActividad() {
        return idActividad;
    }

    public String getIdEstudiante() {
        return idEstudiante;
    }

    public String getRutaArchivoEntrega() {
        return rutaArchivoEntrega;
    }

    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }

    public Double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(double calificacion) {
        if (calificacion < EvaluacionService.CALIFICACION_MINIMA || calificacion > EvaluacionService.CALIFICACION_MAXIMA) {
            throw new IllegalArgumentException(
                    "La calificación debe estar entre " + EvaluacionService.CALIFICACION_MINIMA
                            + " y " + EvaluacionService.CALIFICACION_MAXIMA);
        }
        this.calificacion = calificacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "EntregaEvaluacion{" +
                "idEntrega=" + idEntrega +
                ", idActividad=" + idActividad +
                ", idEstudiante='" + idEstudiante + '\'' +
                ", fechaEntrega=" + fechaEntrega +
                ", calificacion=" + calificacion +
                '}';
    }
}