package sv.gestionacademica.entity;

import java.time.LocalDate;
import sv.gestionacademica.enums.EstadoMatricula;

public class Matricula {

    private int idMatricula;
    private LocalDate fechaMatricula;
    private EstadoMatricula estadoMatricula;
    private Usuario estudiante;
    private Curso curso;

    public Matricula(int idMatricula, LocalDate fechaMatricula,
                     EstadoMatricula estadoMatricula, Usuario estudiante, Curso curso) {
        this.idMatricula = idMatricula;
        this.fechaMatricula = fechaMatricula;
        this.estadoMatricula = estadoMatricula;
        this.estudiante = estudiante;
        this.curso = curso;
    }

    public int getIdMatricula() {
        return idMatricula;
    }

    public void setIdMatricula(int idMatricula) {
        this.idMatricula = idMatricula;
    }

    public LocalDate getFechaMatricula() {
        return fechaMatricula;
    }

    public void setFechaMatricula(LocalDate fechaMatricula) {
        this.fechaMatricula = fechaMatricula;
    }

    public EstadoMatricula getEstadoMatricula() {
        return estadoMatricula;
    }

    public void setEstadoMatricula(EstadoMatricula estadoMatricula) {
        this.estadoMatricula = estadoMatricula;
    }

    public Usuario getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Usuario estudiante) {
        this.estudiante = estudiante;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }
}
