
package sv.gestionacademica.entity;

import sv.gestionacademica.enums.EstadoCurso;

public class Curso {

    private int idCurso;
    private String codigoCurso;
    private String nombreCurso;
    private String descripcion;
    private EstadoCurso estado;
    private int idPeriodo;
    private Integer idInstructor;

    public Curso(String codigoCurso, String nombreCurso, String descripcion, int idPeriodo) {
        this.codigoCurso = codigoCurso;
        this.nombreCurso = nombreCurso;
        this.descripcion = descripcion;
        this.idPeriodo = idPeriodo;
        this.estado = EstadoCurso.INACTIVO;
    }

    public Curso(int idCurso, String codigoCurso, String nombreCurso, String descripcion,
                 EstadoCurso estado, int idPeriodo, Integer idInstructor) {
        this.idCurso = idCurso;
        this.codigoCurso = codigoCurso;
        this.nombreCurso = nombreCurso;
        this.descripcion = descripcion;
        this.estado = estado;
        this.idPeriodo = idPeriodo;
        this.idInstructor = idInstructor;
    }

    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

    public String getCodigoCurso() {
        return codigoCurso;
    }

    public String getNombreCurso() {
        return nombreCurso;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public EstadoCurso getEstado() {
        return estado;
    }

    public int getIdPeriodo() {
        return idPeriodo;
    }

    public Integer getIdInstructor() {
        return idInstructor;
    }


    public void asignarInstructor(int idInstructor) {
        this.idInstructor = idInstructor;
    }

    public void activar() {
        cambiarEstado(EstadoCurso.ACTIVO);
    }

    public void desactivar() {
        cambiarEstado(EstadoCurso.INACTIVO);
    }

    private void cambiarEstado(EstadoCurso nuevoEstado) {
        if (!this.estado.puedeTransicionarA(nuevoEstado)) {
            throw new IllegalStateException(
                    "Transición de estado inválida: " + this.estado + " -> " + nuevoEstado);
        }
        this.estado = nuevoEstado;
    }

    @Override
    public String toString() {
        return "Curso{id=%d, codigo='%s', nombre='%s', estado=%s, idPeriodo=%d, idInstructor=%s}"
                .formatted(idCurso, codigoCurso, nombreCurso, estado, idPeriodo, idInstructor);
    }
}
