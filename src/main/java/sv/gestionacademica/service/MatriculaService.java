package sv.gestionacademica.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.entity.Matricula;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.enums.EstadoMatricula;
import sv.gestionacademica.repository.MatriculaRepository;

public class MatriculaService {

    private final MatriculaRepository matriculaRepository;

    public MatriculaService(MatriculaRepository matriculaRepository) {
        this.matriculaRepository = matriculaRepository;
    }

    public Matricula crearMatricula(Usuario estudiante, Curso curso, EstadoMatricula estado) {
        if (estudiante == null) {
            throw new IllegalArgumentException("El estudiante y el curso son obligatorios");
        }
        if (curso == null) {
            throw new IllegalArgumentException("El estudiante y el curso son obligatorios");
        }
        if (existeMatricula(estudiante, curso)) {
            throw new IllegalStateException("El estudiante ya esta matriculado en este curso");
        }
        Matricula nueva = new Matricula(0, LocalDate.now(), estado, estudiante, curso);
        return matriculaRepository.save(nueva);
    }

    public List<Matricula> listarMatriculas() {
        return matriculaRepository.findAll();
    }

    public List<Matricula> listarPorEstudiante(Usuario estudiante) {
        if (estudiante == null) {
            return new ArrayList<>();
        }
        return matriculaRepository.findByEstudiante(String.valueOf(estudiante.getIdUsuario()));
    }

    public boolean existeMatricula(Usuario estudiante, Curso curso) {
        if (estudiante == null) {
            return false;
        }
        if (curso == null) {
            return false;
        }
        List<Matricula> previas = matriculaRepository.findByEstudiante(String.valueOf(estudiante.getIdUsuario()));
        for (Matricula m : previas) {
            if (m.getCurso() != null) {
                if (m.getCurso().getIdCurso() == curso.getIdCurso()) {
                    if (m.getEstadoMatricula() != EstadoMatricula.CANCELADA) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void cambiarEstado(int idMatricula, EstadoMatricula nuevoEstado) {
        Matricula m = buscarPorId(idMatricula);
        if (m == null) {
            throw new IllegalArgumentException("No existe la matricula con id " + idMatricula);
        }
        m.setEstadoMatricula(nuevoEstado);
        matriculaRepository.update(m);
    }

    public void cancelarMatricula(int idMatricula) {
        cambiarEstado(idMatricula, EstadoMatricula.CANCELADA);
    }

    public Matricula buscarPorId(int idMatricula) {
        return matriculaRepository.findById(idMatricula).orElse(null);
    }
}