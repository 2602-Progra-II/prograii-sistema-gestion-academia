package sv.gestionacademica.Service;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.entity.Matricula;
import sv.gestionacademica.entity.PeriodoAcademico;
import sv.gestionacademica.entity.Rol;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.enums.EstadoCurso;
import sv.gestionacademica.enums.EstadoMatricula;
import sv.gestionacademica.enums.EstadoPeriodo;
import sv.gestionacademica.enums.EstadoUsuario;
import sv.gestionacademica.persistence.DataSourceConfig;
import sv.gestionacademica.repository.*;
import sv.gestionacademica.service.MatriculaService;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@Disabled("Borra todas las tablas de la BD real. Reactivar cuando exista una BD de pruebas separada.")
class MatriculaServiceTest {

    private MatriculaService matriculaService;
    private MatriculaRepository matriculaRepository;

    private Usuario estudiante1;
    private Usuario estudiante2;
    private Curso curso1;
    private Curso curso2;

    @BeforeEach
    void setUp() {
        matriculaRepository = new MatriculaRepositoryImpl();
        matriculaService = new MatriculaService(matriculaRepository);

        // Vaciado total desactivando restricciones FK para limpiar de forma absoluta
        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {

            try {
                stmt.executeUpdate("EXEC sp_msforeachtable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL'");
            } catch (Exception ignored) {}

            String[] tablas = {"pago", "factura", "entrega", "matricula", "actividad", "material_didactico", "curso", "periodo_academico", "usuario", "rol"};
            for (String tabla : tablas) {
                try {
                    stmt.executeUpdate("DELETE FROM " + tabla);
                } catch (Exception ignored) {}
            }

            try {
                stmt.executeUpdate("EXEC sp_msforeachtable 'ALTER TABLE ? CHECK CONSTRAINT ALL'");
            } catch (Exception ignored) {}

        } catch (Exception e) {
            // Silencioso
        }

        // Números aleatorios para garantizar datos 100% únicos por prueba
        int n1 = (int)(Math.random() * 8999 + 1000);
        int n2 = (int)(Math.random() * 8999 + 1000);

        // 1. Roles independientes para evitar restricciones UNIQUE en id_rol
        RolRepository rolRepository = new RolRepositoryImpl();
        Rol rol1 = new Rol(0, "R1_" + n1, "Rol 1");
        Rol rol2 = new Rol(0, "R2_" + n2, "Rol 2");
        rolRepository.save(rol1);
        rolRepository.save(rol2);

        // 2. Usuarios con campos cortos y totalmente únicos
        UsuarioRepository usuarioRepository = new UsuarioRepositoryImpl();
        estudiante1 = new Usuario(0, "u1" + n1, "p1" + n1, "c1" + n1, EstadoUsuario.ACTIVO, rol1);
        estudiante2 = new Usuario(0, "u2" + n2, "p2" + n2, "c2" + n2, EstadoUsuario.ACTIVO, rol2);
        usuarioRepository.guardar(estudiante1);
        usuarioRepository.guardar(estudiante2);

        // 3. Periodo
        PeriodoAcademicoRepository periodoRepository = new PeriodoAcademicoRepositoryImpl();
        PeriodoAcademico periodo = new PeriodoAcademico(0, "P" + n1, LocalDate.now(), LocalDate.now().plusMonths(4), EstadoPeriodo.ACTIVO);
        periodoRepository.guardar(periodo);

        // 4. Cursos
        CursoRepository cursoRepository = new CursoRepositoryImpl();
        curso1 = new Curso(0, "C1" + n1, "Prog I", "Java", EstadoCurso.ACTIVO, periodo.getIdPeriodo(), null);
        curso2 = new Curso(0, "C2" + n2, "BD I", "SQL", EstadoCurso.ACTIVO, periodo.getIdPeriodo(), null);
        cursoRepository.guardar(curso1);
        cursoRepository.guardar(curso2);
    }

    @Test
    @DisplayName("Debería crear una matrícula correctamente en la BD")
    void testCrearMatriculaExitoso() {
        Matricula matricula = matriculaService.crearMatricula(estudiante1, curso1, EstadoMatricula.ACTIVA);

        assertNotNull(matricula);
        assertTrue(matricula.getIdMatricula() > 0, "El ID de la matrícula debe ser generado por la BD");
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
    @DisplayName("Debería listar todas las matrículas desde la BD")
    void testListarMatriculas() {
        matriculaService.crearMatricula(estudiante1, curso1, EstadoMatricula.ACTIVA);
        matriculaService.crearMatricula(estudiante2, curso2, EstadoMatricula.ACTIVA);

        List<Matricula> lista = matriculaService.listarMatriculas();

        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("Debería filtrar las matrículas por estudiante desde la BD")
    void testListarPorEstudiante() {
        matriculaService.crearMatricula(estudiante1, curso1, EstadoMatricula.ACTIVA);
        matriculaService.crearMatricula(estudiante1, curso2, EstadoMatricula.ACTIVA);
        matriculaService.crearMatricula(estudiante2, curso1, EstadoMatricula.ACTIVA);

        List<Matricula> matriculasEstudiante1 = matriculaService.listarPorEstudiante(estudiante1);

        assertEquals(2, matriculasEstudiante1.size());
    }

    @Test
    @DisplayName("Debería cambiar el estado de la matrícula correctamente en la BD")
    void testCambiarEstadoExitoso() {
        Matricula m = matriculaService.crearMatricula(estudiante1, curso1, EstadoMatricula.PENDIENTE_PAGO);

        matriculaService.cambiarEstado(m.getIdMatricula(), EstadoMatricula.ACTIVA);

        Matricula recuperada = matriculaService.buscarPorId(m.getIdMatricula());
        assertEquals(EstadoMatricula.ACTIVA, recuperada.getEstadoMatricula());
    }

    @Test
    @DisplayName("Debería cancelar la matrícula correctamente en la BD")
    void testCancelarMatriculaExitoso() {
        Matricula m = matriculaService.crearMatricula(estudiante1, curso1, EstadoMatricula.ACTIVA);

        matriculaService.cancelarMatricula(m.getIdMatricula());

        Matricula recuperada = matriculaService.buscarPorId(m.getIdMatricula());
        assertEquals(EstadoMatricula.CANCELADA, recuperada.getEstadoMatricula());
    }

    @Test
    @DisplayName("Debería buscar matrícula por ID o retornar null si no existe")
    void testBuscarPorId() {
        Matricula m = matriculaService.crearMatricula(estudiante1, curso1, EstadoMatricula.ACTIVA);

        assertNotNull(matriculaService.buscarPorId(m.getIdMatricula()));
        assertNull(matriculaService.buscarPorId(999));
    }
}