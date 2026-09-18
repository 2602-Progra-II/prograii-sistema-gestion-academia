package sv.gestionacademica.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import sv.gestionacademica.entity.Matricula;
import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.enums.EstadoMatricula;

public class MatriculaService {

    private List<Matricula> matriculas;
    private int siguienteId;

    public MatriculaService() {
        this.matriculas = new ArrayList<>();
        this.siguienteId = 1;
    }

    public Matricula crearMatricula(Usuario estudiante, Curso curso, EstadoMatricula estado) {
        if (estudiante == null || curso == null) {
            throw new IllegalArgumentException("El estudiante y el curso son obligatorios");
        }
        if (existeMatricula(estudiante, curso)) {
            throw new IllegalStateException("El estudiante ya está matriculado en este curso");
        }
        Matricula nueva = new Matricula(siguienteId++, LocalDate.now(), estado, estudiante, curso);
        matriculas.add(nueva);
        return nueva;
    }

    public List<Matricula> listarMatriculas() {
        return new ArrayList<>(matriculas);
    }

    public List<Matricula> listarPorEstudiante(Usuario estudiante) {
        List<Matricula> resultado = new ArrayList<>();
        for (Matricula m : matriculas) {
            if (m.getEstudiante().equals(estudiante)) {
                resultado.add(m);
            }
        }
        return resultado;
    }

    public boolean existeMatricula(Usuario estudiante, Curso curso) {
        for (Matricula m : matriculas) {
            if (m.getEstudiante().equals(estudiante) && m.getCurso().equals(curso)
                    && m.getEstadoMatricula() != EstadoMatricula.CANCELADA) {
                return true;
            }
        }
        return false;
    }

    public void cambiarEstado(int idMatricula, EstadoMatricula nuevoEstado) {
        Matricula m = buscarPorId(idMatricula);
        if (m == null) {
            throw new IllegalArgumentException("No existe la matrícula con id " + idMatricula);
        }
        m.setEstadoMatricula(nuevoEstado);
    }

    public void cancelarMatricula(int idMatricula) {
        cambiarEstado(idMatricula, EstadoMatricula.CANCELADA);
    }

    public Matricula buscarPorId(int idMatricula) {
        for (Matricula m : matriculas) {
            if (m.getIdMatricula() == idMatricula) {
                return m;
            }
        }
        return null;
    }
}
