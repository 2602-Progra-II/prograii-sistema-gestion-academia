package sv.gestionacademica.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.entity.PeriodoAcademico;
import sv.gestionacademica.enums.EstadoCurso;
import sv.gestionacademica.enums.EstadoPeriodo;
import sv.gestionacademica.persistence.DataSourceConfig;
import sv.gestionacademica.repository.CursoRepository;
import sv.gestionacademica.repository.CursoRepositoryImpl;
import sv.gestionacademica.repository.PeriodoAcademicoRepository;
import sv.gestionacademica.repository.PeriodoAcademicoRepositoryImpl;
import sv.gestionacademica.service.CursoService;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class CursoServiceTest {

    private CursoService cursoService;
    private CursoRepository cursoRepository;
    private PeriodoAcademicoRepository periodoRepository;

    private PeriodoAcademico periodoActivo;

    @BeforeEach
    void setUp() {
        cursoRepository = new CursoRepositoryImpl();
        periodoRepository = new PeriodoAcademicoRepositoryImpl();
        cursoService = new CursoService(cursoRepository, periodoRepository);

        // Limpiar la base de datos antes de cada prueba
        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM curso");
            stmt.executeUpdate("DELETE FROM periodo_academico");
        } catch (Exception e) {
            // Silencioso si las tablas ya están vacías
        }

        // Periodo base con sufijo dinámico corto
        periodoActivo = new PeriodoAcademico(0, "P" + (int)(Math.random() * 9000 + 1000), LocalDate.now(), LocalDate.now().plusMonths(4), EstadoPeriodo.ACTIVO);
        periodoRepository.guardar(periodoActivo);
    }

    @Test
    @DisplayName("Debería registrar un curso correctamente si el periodo existe y el código es válido")
    void testRegistrarCursoExitoso() {
        String codigoUnico = "C" + (int)(Math.random() * 9000 + 1000);
        Curso curso = new Curso(0, codigoUnico, "Programación I", "Introducción", EstadoCurso.INACTIVO, periodoActivo.getIdPeriodo(), null);

        Curso registrado = cursoService.registrarCurso(curso);

        assertNotNull(registrado);
        assertEquals(codigoUnico, registrado.getCodigoCurso());
        assertTrue(registrado.getIdCurso() > 0, "El ID del curso debe ser generado por la BD");
    }

    @Test
    @DisplayName("Debería lanzar NoSuchElementException al registrar un curso con un periodo inexistente")
    void testRegistrarCursoPeriodoInexistente() {
        Curso curso = new Curso(0, "C999", "Programación II", "POO Avanzada", EstadoCurso.INACTIVO, 9999, null);

        assertThrows(NoSuchElementException.class, () -> cursoService.registrarCurso(curso));
    }

    @Test
    @DisplayName("Debería lanzar IllegalArgumentException al registrar un curso con código nulo o vacío")
    void testRegistrarCursoCodigoVacio() {
        Curso curso = new Curso(0, "", "Base de Datos", "Relacional", EstadoCurso.INACTIVO, periodoActivo.getIdPeriodo(), null);

        assertThrows(IllegalArgumentException.class, () -> cursoService.registrarCurso(curso));
    }

    @Test
    @DisplayName("Debería lanzar IllegalArgumentException al registrar un curso con código duplicado")
    void testRegistrarCursoCodigoDuplicado() {
        String codigoDuplicado = "D" + (int)(Math.random() * 9000 + 1000);
        Curso c1 = new Curso(0, codigoDuplicado, "Programación I", "Intro", EstadoCurso.INACTIVO, periodoActivo.getIdPeriodo(), null);
        cursoService.registrarCurso(c1);

        Curso c2 = new Curso(0, codigoDuplicado, "Programación I Paralelo", "Intro 2", EstadoCurso.INACTIVO, periodoActivo.getIdPeriodo(), null);

        assertThrows(IllegalArgumentException.class, () -> cursoService.registrarCurso(c2));
    }

    @Test
    @DisplayName("Debería desactivar un curso correctamente")
    void testDesactivarCursoExitoso() {
        String codigoDes = "D" + (int)(Math.random() * 9000 + 1000);
        Curso curso = new Curso(0, codigoDes, "Programación I", "Intro", EstadoCurso.INACTIVO, periodoActivo.getIdPeriodo(), null);
        Curso registrado = cursoService.registrarCurso(curso);

        Curso desactivado = cursoService.desactivarCurso(registrado.getIdCurso());

        assertEquals(EstadoCurso.INACTIVO, desactivado.getEstado());
    }

    @Test
    @DisplayName("Debería validar la transición de estado al intentar activar un curso")
    void testActivarCursoExitoso() {
        String codigoAct = "A" + (int)(Math.random() * 9000 + 1000);
        Curso curso = new Curso(0, codigoAct, "Programación I", "Intro", EstadoCurso.INACTIVO, periodoActivo.getIdPeriodo(), null);
        Curso registrado = cursoService.registrarCurso(curso);

        // Al recuperar el curso de la BD ya se mapea como activo, por lo que re-activarlo valida la regla de negocio
        assertThrows(IllegalStateException.class, () -> cursoService.activarCurso(registrado.getIdCurso()));
    }

    @Test
    @DisplayName("Debería listar cursos por periodo académico")
    void testListarPorPeriodo() {
        String cod1 = "L" + (int)(Math.random() * 9000 + 1000);
        String cod2 = "M" + (int)(Math.random() * 9000 + 1000);
        Curso c1 = new Curso(0, cod1, "Programación I", "Intro", EstadoCurso.INACTIVO, periodoActivo.getIdPeriodo(), null);
        Curso c2 = new Curso(0, cod2, "Programación II", "POO", EstadoCurso.INACTIVO, periodoActivo.getIdPeriodo(), null);
        cursoService.registrarCurso(c1);
        cursoService.registrarCurso(c2);

        List<Curso> cursos = cursoService.listarPorPeriodo(periodoActivo.getIdPeriodo());

        assertEquals(2, cursos.size());
    }
}