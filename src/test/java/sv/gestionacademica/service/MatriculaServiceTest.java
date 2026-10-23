package sv.gestionacademica.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.entity.Matricula;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.enums.EstadoMatricula;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatriculaServiceTest {

    private MatriculaService matriculaService;
    private Usuario estudiante1;
    private Usuario estudiante2;
    private Curso curso1;
    private Curso curso2;

    @BeforeEach
    void setUp() {
        matriculaService = new MatriculaService();

        // Instanciamos objetos base para las pruebas
        estudiante1 = new Usuario(1, "Juan", "Pérez", "pass123", null, null);
        estudiante2 = new Usuario(2, "Maria", "Gómez", "pass456", null, null);

        curso1 = new Curso("INF101", "Programación I", "Intro a Java", 1);
        curso2 = new Curso("INF102", "Base de Datos", "Relacional", 1);
    }

    @Test
    @DisplayName("Debería crear una matrícula correctamente")
    void testCrearMatriculaExitoso() {
        Matricula matricula = matriculaService.crearMatricula(estudiante1, curso1, EstadoMatricula.ACTIVA);

        assertNotNull(matricula);
        assertEquals(1, matricula.getIdMatricula());
        assertEquals(estudiante1, matricula.getEstudiante());
        assertEquals(curso1, matricula.getCurso());
        assertEquals(EstadoMatricula.ACTIVA, matricula.getEstadoMatricula());
    }

    @Test
    @DisplayName("Debería lanzar IllegalArgumentException al intentar matricular con estudiante o curso nulo")
    void testCrearMatriculaParametrosNulos() {
        assertThrows(IllegalArgumentException.class, () ->
                matriculaService.crearMatricula(null, curso1, EstadoMatricula.ACTIVA));

        assertThrows(IllegalArgumentException.class, () ->
                matriculaService.crearMatricula(estudiante1, null, EstadoMatricula.ACTIVA));
    }

    @Test
    @DisplayName("Debería lanzar IllegalStateException al registrar una matrícula duplicada activa")
    void testCrearMatriculaDuplicada() {
        matriculaService.crearMatricula(estudiante1, curso1, EstadoMatricula.ACTIVA);

        assertThrows(IllegalStateException.class, () ->
                matriculaService.crearMatricula(estudiante1, curso1, EstadoMatricula.PENDIENTE_PAGO));
    }

    @Test
    @DisplayName("Debería permitir matricular de nuevo si la matrícula previa fue CANCELADA")
    void testCrearMatriculaDespuesDeCancelada() {
        Matricula m1 = matriculaService.crearMatricula(estudiante1, curso1, EstadoMatricula.ACTIVA);
        matriculaService.cancelarMatricula(m1.getIdMatricula());

        assertDoesNotThrow(() ->
                matriculaService.crearMatricula(estudiante1, curso1, EstadoMatricula.ACTIVA));
    }

    @Test
    @DisplayName("Debería listar todas las matrículas")
    void testListarMatriculas() {
        matriculaService.crearMatricula(estudiante1, curso1, EstadoMatricula.ACTIVA);
        matriculaService.crearMatricula(estudiante2, curso2, EstadoMatricula.ACTIVA);

        List<Matricula> lista = matriculaService.listarMatriculas();

        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("Debería filtrar las matrículas por estudiante")
    void testListarPorEstudiante() {
        matriculaService.crearMatricula(estudiante1, curso1, EstadoMatricula.ACTIVA);
        matriculaService.crearMatricula(estudiante1, curso2, EstadoMatricula.ACTIVA);
        matriculaService.crearMatricula(estudiante2, curso1, EstadoMatricula.ACTIVA);

        List<Matricula> matriculasEstudiante1 = matriculaService.listarPorEstudiante(estudiante1);

        assertEquals(2, matriculasEstudiante1.size());
    }

    @Test
    @DisplayName("Debería cambiar el estado de la matrícula correctamente")
    void testCambiarEstadoExitoso() {
        Matricula m = matriculaService.crearMatricula(estudiante1, curso1, EstadoMatricula.PENDIENTE_PAGO);

        matriculaService.cambiarEstado(m.getIdMatricula(), EstadoMatricula.ACTIVA);

        assertEquals(EstadoMatricula.ACTIVA, m.getEstadoMatricula());
    }

    @Test
    @DisplayName("Debería lanzar IllegalArgumentException al intentar cambiar estado de matrícula inexistente")
    void testCambiarEstadoInexistente() {
        assertThrows(IllegalArgumentException.class, () ->
                matriculaService.cambiarEstado(99, EstadoMatricula.CANCELADA));
    }

    @Test
    @DisplayName("Debería cancelar la matrícula correctamente")
    void testCancelarMatriculaExitoso() {
        Matricula m = matriculaService.crearMatricula(estudiante1, curso1, EstadoMatricula.ACTIVA);

        matriculaService.cancelarMatricula(m.getIdMatricula());

        assertEquals(EstadoMatricula.CANCELADA, m.getEstadoMatricula());
    }

    @Test
    @DisplayName("Debería buscar matrícula por ID o retornar null si no existe")
    void testBuscarPorId() {
        Matricula m = matriculaService.crearMatricula(estudiante1, curso1, EstadoMatricula.ACTIVA);

        assertNotNull(matriculaService.buscarPorId(m.getIdMatricula()));
        assertNull(matriculaService.buscarPorId(999));
    }
}
