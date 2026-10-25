package sv.gestionacademica.entity;

import sv.gestionacademica.enums.TipoActividad;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Actividad evaluativa de un curso. Clase abstracta: solo existen sus especializaciones
 * (Tarea, Examen, Proyecto), cada una con su propia regla para la nota final.
 *
 * Polimorfismo: el sistema trabaja con referencias de tipo ActividadEvaluativa y llama a
 * calcularNotaFinal(nota, fechaEntrega) / permiteEntregaTardia() sin saber la subclase concreta.
 */
public abstract class ActividadEvaluativa implements Serializable, Identificable<String> {
    private static final long serialVersionUID = 1L;

    private String id;
    private String nombre;
    private double ponderacion;
    private String fechaEntrega;
    private String descripcion;
    /** Curso al que pertenece (columna actividad_evaluativa.id_curso). */
    private int idCurso;

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

    /** Nota final (0-10) de una entrega hecha a tiempo. Cada especializacion define su regla. */
    public abstract double calcularNotaFinal(double notaObtenida);

    /**
     * Nota final (0-10) considerando CUANDO se entrego. Por defecto la fecha no influye;
     * Tarea y Proyecto lo sobrescriben (penalizacion por atraso / bonificacion a tiempo).
     */
    public double calcularNotaFinal(double notaObtenida, LocalDate fechaEntrega) {
        return calcularNotaFinal(notaObtenida);
    }

    /** Si se aceptan entregas despues de la fecha limite. Por defecto no. */
    public boolean permiteEntregaTardia() {
        return false;
    }

    /** Tipo persistido en la BD. Las subclases lo sobrescriben; una subclase anonima cuenta como TAREA. */
    public TipoActividad getTipo() {
        return TipoActividad.TAREA;
    }

    /** Puntos que la nota final aporta a la nota del curso: nota * ponderacion / 100. */
    public final double calcularAporte(double notaFinal) {
        return notaFinal * ponderacion / 100.0;
    }

    public LocalDate getFechaLimite() {
        return fechaEntrega == null || fechaEntrega.isBlank() ? null : LocalDate.parse(fechaEntrega);
    }

    /** true si 'fecha' es posterior a la fecha limite. Sin fecha limite nunca vence. */
    public boolean estaVencidaPara(LocalDate fecha) {
        LocalDate limite = getFechaLimite();
        return limite != null && fecha != null && fecha.isAfter(limite);
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

    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

    /** true si la fecha limite ya paso (respecto de hoy). */
    public boolean estaVencida() {
        return estaVencidaPara(LocalDate.now());
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
