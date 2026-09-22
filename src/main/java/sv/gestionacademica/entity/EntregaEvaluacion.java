package sv.gestionacademica.entity;

import java.time.LocalDate;

public class EntregaEvaluacion {

    private int idEntrega;
    private String rutaArchivoEntrega;
    private LocalDate fechaEntrega;
    private double calificacion;
    private String observaciones;
    private int idActividad;
    private String idEstudiante;

    public EntregaEvaluacion() {
    }

    public EntregaEvaluacion(int idEntrega, String rutaArchivoEntrega, LocalDate fechaEntrega, 
                             double calificacion, String observaciones, int idActividad, String idEstudiante) {
        this.idEntrega = idEntrega;
        this.rutaArchivoEntrega = rutaArchivoEntrega;
        this.fechaEntrega = fechaEntrega;
        this.calificacion = calificacion;
        this.observaciones = observaciones;
        this.idActividad = idActividad;
        this.idEstudiante = idEstudiante;
    }

    public boolean estaCalificada() {
        return this.calificacion >= 0.0 && this.observaciones != null && !this.observaciones.isBlank();
    }

    public int getIdEntrega() {
        return idEntrega;
    }

    public void setIdEntrega(int idEntrega) {
        this.idEntrega = idEntrega;
    }

    public String getRutaArchivoEntrega() {
        return rutaArchivoEntrega;
    }

    public void setRutaArchivoEntrega(String rutaArchivoEntrega) {
        this.rutaArchivoEntrega = rutaArchivoEntrega;
    }

    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDate fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(double calificacion) {
        this.calificacion = calificacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public int getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(int idActividad) {
        this.idActividad = idActividad;
    }

    public String getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(String idEstudiante) {
        this.idEstudiante = idEstudiante;
    }
}
