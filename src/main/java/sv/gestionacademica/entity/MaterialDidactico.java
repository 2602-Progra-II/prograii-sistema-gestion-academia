package sv.gestionacademica.entity;

import java.io.Serializable;
import java.util.Objects;

public class MaterialDidactico implements Serializable, Identificable<String> {
    private static final long serialVersionUID = 1L;

    private String id;
    private String titulo;
    private String descripcion;
    private String urlEnlace;
    /** Curso al que pertenece (columna material_didactico.id_curso). */
    private int idCurso;

    public MaterialDidactico() {
    }

    public MaterialDidactico(String id, String titulo, String descripcion, String urlEnlace) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.urlEnlace = urlEnlace;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUrlEnlace() {
        return urlEnlace;
    }

    public void setUrlEnlace(String urlEnlace) {
        this.urlEnlace = urlEnlace;
    }

    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaterialDidactico that = (MaterialDidactico) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MaterialDidactico [id=" + id + ", titulo=" + titulo + ", descripcion=" + descripcion + ", urlEnlace=" + urlEnlace + "]";
    }
}