package sv.gestionacademica.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.entity.PeriodoAcademico;
import sv.gestionacademica.enums.EstadoCurso;
import sv.gestionacademica.enums.EstadoPeriodo;
import sv.gestionacademica.repository.CursoRepository;
import sv.gestionacademica.repository.PeriodoAcademicoRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CursoServiceTest {

    private CursoService cursoService;
    private FakeCursoRepository cursoRepository;
    private FakePeriodoAcademicoRepository periodoRepository;

    private PeriodoAcademico periodoActivo;

    @BeforeEach
    void setUp() {
        cursoRepository = new FakeCursoRepository();
        periodoRepository = new FakePeriodoAcademicoRepository();
        cursoService = new CursoService(cursoRepository, periodoRepository);

        // Preparamos un periodo base para asociar los cursos de prueba
        periodoActivo = new PeriodoAcademico(1, "Ciclo 01-2026", LocalDate.now(), LocalDate.now().plusMonths(4), EstadoPeriodo.ACTIVO);
        periodoRepository.guardar(periodoActivo);
    }

    @Test
    @DisplayName("Debería registrar un curso correctamente si el periodo existe y el código es válido")
    void testRegistrarCursoExitoso() {
        Curso curso = new Curso("INF101", "Programación I", "Introducción a la programación", 1);

        Curso registrado = cursoService.registrarCurso(curso);

        assertNotNull(registrado);
        assertEquals("INF101", registrado.getCodigoCurso());
        assertEquals(EstadoCurso.INACTIVO, registrado.getEstado());
    }

    @Test
    @DisplayName("Debería lanzar NoSuchElementException al registrar un curso con un periodo inexistente")
    void testRegistrarCursoPeriodoInexistente() {
        Curso curso = new Curso("INF102", "Programación II", "POO Avanzada", 99);

        assertThrows(NoSuchElementException.class, () -> cursoService.registrarCurso(curso));
    }

    @Test
    @DisplayName("Debería lanzar IllegalArgumentException al registrar un curso con código nulo o vacío")
    void testRegistrarCursoCodigoVacio() {
        Curso curso = new Curso("", "Base de Datos", "Relacional", 1);

        assertThrows(IllegalArgumentException.class, () -> cursoService.registrarCurso(curso));
    }

    @Test
    @DisplayName("Debería lanzar IllegalArgumentException al registrar un curso con código duplicado")
    void testRegistrarCursoCodigoDuplicado() {
        Curso c1 = new Curso("INF101", "Programación I", "Intro", 1);
        cursoService.registrarCurso(c1);

        Curso c2 = new Curso("INF101", "Programación I Paraleo", "Intro 2", 1);

        assertThrows(IllegalArgumentException.class, () -> cursoService.registrarCurso(c2));
    }

    @Test
    @DisplayName("Debería asignar un instructor correctamente si el periodo está activo")
    void testAsignarInstructorExitoso() {
        Curso curso = new Curso("INF101", "Programación I", "Intro", 1);
        Curso registrado = cursoService.registrarCurso(curso);

        Curso actualizado = cursoService.asignarInstructor(registrado.getIdCurso(), 500);

        assertEquals(500, actualizado.getIdInstructor());
    }

    @Test
    @DisplayName("Debería lanzar IllegalStateException al asignar instructor a un curso cuyo periodo está finalizado")
    void testAsignarInstructorPeriodoFinalizado() {
        PeriodoAcademico periodoFin = new PeriodoAcademico(2, "Ciclo 02-2025", LocalDate.now().minusMonths(6), LocalDate.now().minusMonths(1), EstadoPeriodo.FINALIZADO);
        periodoRepository.guardar(periodoFin);

        Curso curso = new Curso("INF103", "Estructura de Datos", "Avanzado", 2);
        Curso registrado = cursoService.registrarCurso(curso);

        assertThrows(IllegalStateException.class, () -> cursoService.asignarInstructor(registrado.getIdCurso(), 500));
    }

    @Test
    @DisplayName("Debería activar un curso correctamente")
    void testActivarCursoExitoso() {
        Curso curso = new Curso("INF101", "Programación I", "Intro", 1);
        Curso registrado = cursoService.registrarCurso(curso);

        Curso activado = cursoService.activarCurso(registrado.getIdCurso());

        assertEquals(EstadoCurso.ACTIVO, activado.getEstado());
    }

    @Test
    @DisplayName("Debería desactivar un curso correctamente")
    void testDesactivarCursoExitoso() {
        Curso curso = new Curso("INF101", "Programación I", "Intro", 1);
        Curso registrado = cursoService.registrarCurso(curso);
        cursoService.activarCurso(registrado.getIdCurso());

        Curso desactivado = cursoService.desactivarCurso(registrado.getIdCurso());

        assertEquals(EstadoCurso.INACTIVO, desactivado.getEstado());
    }

    @Test
    @DisplayName("Debería listar cursos por periodo académico")
    void testListarPorPeriodo() {
        Curso c1 = new Curso("INF101", "Programación I", "Intro", 1);
        Curso c2 = new Curso("INF102", "Programación II", "POO", 1);
        cursoService.registrarCurso(c1);
        cursoService.registrarCurso(c2);

        List<Curso> cursos = cursoService.listarPorPeriodo(1);

        assertEquals(2, cursos.size());
    }

    // --- Fake Repository de Cursos ---
    private static class FakeCursoRepository implements CursoRepository {
        private final Map<Integer, Curso> datos = new HashMap<>();
        private int idSequence = 1;

        @Override
        public Curso guardar(Curso c) {
            if (c.getIdCurso() == 0) {
                c.setIdCurso(idSequence++);
            }
            datos.put(c.getIdCurso(), c);
            return c;
        }

        @Override
        public Optional<Curso> buscarPorId(int idCurso) {
            return Optional.ofNullable(datos.get(idCurso));
        }

        @Override
        public Optional<Curso> buscarPorCodigo(String codigoCurso) {
            return datos.values().stream()
                    .filter(c -> c.getCodigoCurso() != null && c.getCodigoCurso().equalsIgnoreCase(codigoCurso))
                    .findFirst();
        }

        @Override
        public List<Curso> listarTodos() {
            return new ArrayList<>(datos.values());
        }

        @Override
        public List<Curso> listarPorPeriodo(int idPeriodo) {
            return datos.values().stream()
                    .filter(c -> c.getIdPeriodo() == idPeriodo)
                    .collect(Collectors.toList());
        }

        @Override
        public List<Curso> listarPorInstructor(int idInstructor) {
            return datos.values().stream()
                    .filter(c -> c.getIdInstructor() != null && c.getIdInstructor() == idInstructor)
                    .collect(Collectors.toList());
        }

        @Override
        public Curso actualizar(Curso c) {
            datos.put(c.getIdCurso(), c);
            return c;
        }

        @Override
        public void eliminar(int idCurso) {
            datos.remove(idCurso);
        }
    }

    // --- Fake Repository de Periodos Académicos ---
    private static class FakePeriodoAcademicoRepository implements PeriodoAcademicoRepository {
        private final Map<Integer, PeriodoAcademico> datos = new HashMap<>();

        @Override
        public PeriodoAcademico guardar(PeriodoAcademico p) {
            datos.put(p.getIdPeriodo(), p);
            return p;
        }

        @Override
        public Optional<PeriodoAcademico> buscarPorId(int idPeriodo) {
            return Optional.ofNullable(datos.get(idPeriodo));
        }

        @Override
        public Optional<PeriodoAcademico> buscarPorNombre(String nombrePeriodo) {
            return datos.values().stream()
                    .filter(p -> p.getNombrePeriodo() != null && p.getNombrePeriodo().equalsIgnoreCase(nombrePeriodo))
                    .findFirst();
        }

        @Override
        public List<PeriodoAcademico> listarTodos() {
            return new ArrayList<>(datos.values());
        }

        @Override
        public List<PeriodoAcademico> listarVigentes() {
            return datos.values().stream()
                    .filter(p -> p.getEstado() == EstadoPeriodo.ACTIVO)
                    .collect(Collectors.toList());
        }

        @Override
        public PeriodoAcademico actualizar(PeriodoAcademico p) {
            datos.put(p.getIdPeriodo(), p);
            return p;
        }

        @Override
        public void eliminar(int idPeriodo) {
            datos.remove(idPeriodo);
        }
    }
}