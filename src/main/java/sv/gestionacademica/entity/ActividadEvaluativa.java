package sv.gestionacademica.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class ActividadEvaluativa implements Serializable, Identificable<String> {
    private static final long serialVersionUID = 1L;

    private String id;
    private String nombre;
    private double ponderacion;
    private String fechaEntrega;
    private String descripcion;

    public ActividadEvaluativa() {
    }

    public ActividadEvaluativa(String id, String nombre, double ponderacion, String fechaEntrega) {
        this.id = id;
        this.nombre = nombre;
        this.ponderacion = ponderacion;
        this.fechaEntrega = fechaEntrega;
    }

    // Constructor de compatibilidad para subclases (Examen, Proyecto, Tarea)
    public ActividadEvaluativa(int idActividad, String nombreActividad, String descripcion, LocalDate fechaLimite, double ponderacion) {
        this.id = String.valueOf(idActividad);
        this.nombre = nombreActividad;
        this.descripcion = descripcion;
        this.fechaEntrega = fechaLimite != null ? fechaLimite.toString() : null;
        this.ponderacion = ponderacion;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPonderacion() {
        return ponderacion;
    }

    public void setPonderacion(double ponderacion) {
        this.ponderacion = ponderacion;
    }

    public String getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(String fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Metodos de compatibilidad para subclases
    public boolean estaVencida() {
        return false;
    }

    public void mostrarDetalles() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActividadEvaluativa that = (ActividadEvaluativa) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ActividadEvaluativa{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", ponderacion=" + ponderacion +
                ", fechaEntrega='" + fechaEntrega + '\'' +
                '}';
    }
}
