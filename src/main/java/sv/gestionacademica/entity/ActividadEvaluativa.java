package sv.gestionacademica.entity;

import java.time.LocalDate;

public abstract class ActividadEvaluativa {

    private int idActividad;
    private String nombreActividad;
    private String descripcion;
    private LocalDate fechaLimite;
    private double ponderacion;

    public ActividadEvaluativa() {
    }

    public ActividadEvaluativa(int idActividad, String nombreActividad, String descripcion,
                               LocalDate fechaLimite, double ponderacion) {
        this.idActividad = idActividad;
        this.nombreActividad = nombreActividad;
        this.descripcion = descripcion;
        this.fechaLimite = fechaLimite;
        this.ponderacion = ponderacion;
    }

    public boolean estaVencida() {
        return LocalDate.now().isAfter(fechaLimite);
    }

    // Cada tipo de actividad calcula su nota final a su manera
    public abstract double calcularNotaFinal(double notaObtenida);

    public int getIdActividad() { return idActividad; }
    public void setIdActividad(int idActividad) { this.idActividad = idActividad; }

    public String getNombreActividad() { return nombreActividad; }
    public void setNombreActividad(String nombreActividad) { this.nombreActividad = nombreActividad; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }

    public double getPonderacion() { return ponderacion; }
    public void setPonderacion(double ponderacion) { this.ponderacion = ponderacion; }
}