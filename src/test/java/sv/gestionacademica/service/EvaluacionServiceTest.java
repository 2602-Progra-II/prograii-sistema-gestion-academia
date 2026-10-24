package sv.gestionacademica.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sv.gestionacademica.entity.ActividadEvaluativa;
import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.entity.EntregaEvaluacion;
import sv.gestionacademica.entity.PeriodoAcademico;
import sv.gestionacademica.entity.Rol;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.enums.EstadoCurso;
import sv.gestionacademica.enums.EstadoPeriodo;
import sv.gestionacademica.enums.EstadoUsuario;
import sv.gestionacademica.persistence.DataSourceConfig;
import sv.gestionacademica.repository.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EvaluacionServiceTest {

    private EvaluacionService evaluacionService;
    private EntregaRepository entregaRepository;

    private Usuario estudiante;
    private Usuario instructor;
    private ActividadEvaluativa actividad1;
    private ActividadEvaluativa actividad2;

    @BeforeEach
    void setUp() {
        entregaRepository = new EntregaRepositoryImpl();
        evaluacionService = new EvaluacionService(entregaRepository);

        // Vaciado total desactivando restricciones FK para limpiar de forma absoluta
        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {

            try { stmt.executeUpdate("EXEC sp_msforeachtable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL'"); } catch (Exception ignored) {}

            String[] tablas = {"pago", "factura", "entrega_evaluacion", "actividad_evaluativa", "matricula", "material_didactico", "curso", "periodo_academico", "usuario", "rol"};
            for (String tabla : tablas) {
                try { stmt.executeUpdate("DELETE FROM " + tabla); } catch (Exception ignored) {}
            }

            try { stmt.executeUpdate("EXEC sp_msforeachtable 'ALTER TABLE ? CHECK CONSTRAINT ALL'"); } catch (Exception ignored) {}
        } catch (Exception ignored) {}

        // Números aleatorios independientes
        int n1 = (int)(Math.random() * 89999 + 10000);
        int n2 = (int)(Math.random() * 89999 + 10000);

        // 1. Roles
        RolRepository rolRepo = new RolRepositoryImpl();
        Rol rolEst = new Rol(0, "RE_" + n1, "Estudiante");
        Rol rolIns = new Rol(0, "RI_" + n2, "Instructor");
        rolRepo.save(rolEst);
        rolRepo.save(rolIns);

        // 2. Usuarios con datos 100% únicos
        UsuarioRepository userRepo = new UsuarioRepositoryImpl();
        estudiante = new Usuario(0, "e" + n1, "Perez", "p" + n1, EstadoUsuario.ACTIVO, rolEst);
        instructor = new Usuario(0, "i" + n2, "Lopez", "p" + n2, EstadoUsuario.ACTIVO, rolIns);
        userRepo.guardar(estudiante);
        userRepo.guardar(instructor);

        // 3. Periodo Académico y Curso base
        PeriodoAcademicoRepository periodoRepo = new PeriodoAcademicoRepositoryImpl();
        PeriodoAcademico periodo = new PeriodoAcademico(0, "P_" + n1, LocalDate.now(), LocalDate.now().plusMonths(4), EstadoPeriodo.ACTIVO);
        periodoRepo.guardar(periodo);

        CursoRepository cursoRepo = new CursoRepositoryImpl();
        Curso curso = new Curso(0, "C_" + n1, "Curso Evaluaciones", "Descripcion", EstadoCurso.ACTIVO, periodo.getIdPeriodo(), null);
        cursoRepo.guardar(curso);

        // 4. Actividades Evaluativas base vinculadas al curso
        ActividadRepository actRepo = new ActividadRepositoryImpl();
        ActividadEvaluativa actBase1 = new ActividadEvaluativa(0, "Tarea 1 " + n1, "Desc", LocalDate.now().plusDays(7), 20.0) {
            @Override public double calcularNotaFinal(double nota) { return (nota * 20.0) / 100.0; }
        };
        ActividadEvaluativa actBase2 = new ActividadEvaluativa(0, "Tarea 2 " + n2, "Desc", LocalDate.now().plusDays(7), 20.0) {
            @Override public double calcularNotaFinal(double nota) { return (nota * 20.0) / 100.0; }
        };

        actividad1 = actRepo.save(actBase1);
        actividad2 = actRepo.save(actBase2);
    }

    @Test
    @DisplayName("Debería registrar la entrega de una actividad en SQL Server")
    void testEntregarActividadExitoso() {
        EntregaEvaluacion entrega = new EntregaEvaluacion();
        entrega.setIdActividad(Integer.parseInt(actividad1.getId()));
        entrega.setRutaArchivoEntrega("/uploads/tarea1.pdf");

        EntregaEvaluacion resultado = evaluacionService.entregarActividad(entrega, estudiante);

        assertNotNull(resultado);
        assertTrue(resultado.getIdEntrega() > 0, "El ID de la entrega debe ser autogenerado por SQL Server");
        assertEquals(String.valueOf(estudiante.getIdUsuario()), resultado.getIdEstudiante());
        assertNotNull(resultado.getFechaEntrega());
    }

    @Test
    @DisplayName("Debería calificar una entrega registrada en SQL Server")
    void testCalificarActividadExitoso() {
        EntregaEvaluacion entrega = new EntregaEvaluacion(0, "/uploads/tarea1.pdf", LocalDate.now(), -1.0, null, Integer.parseInt(actividad1.getId()), String.valueOf(estudiante.getIdUsuario()));
        entregaRepository.guardar(entrega);

        entrega.setCalificacion(9.5);
        entrega.setObservaciones("Excelente trabajo");

        evaluacionService.calificarActividad(entrega, instructor);

        EntregaEvaluacion recuperada = entregaRepository.buscarPorId(entrega.getIdEntrega()).orElse(null);
        assertNotNull(recuperada);
        assertTrue(recuperada.estaCalificada());
        assertEquals(9.5, recuperada.getCalificacion());
    }

    @Test
    @DisplayName("Debería consultar calificaciones por estudiante desde SQL Server")
    void testConsultarCalificaciones() {
        // Usamos dos actividades distintas para evitar colisiones de llaves únicas (id_estudiante, id_actividad)
        EntregaEvaluacion e1 = new EntregaEvaluacion(0, "/file1.pdf", LocalDate.now(), 8.0, "Buen trabajo", Integer.parseInt(actividad1.getId()), String.valueOf(estudiante.getIdUsuario()));
        EntregaEvaluacion e2 = new EntregaEvaluacion(0, "/file2.pdf", LocalDate.now(), 10.0, "Excelente", Integer.parseInt(actividad2.getId()), String.valueOf(estudiante.getIdUsuario()));

        entregaRepository.guardar(e1);
        entregaRepository.guardar(e2);

        List<EntregaEvaluacion> lista = evaluacionService.consultarCalificaciones(estudiante);

        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("Debería validar la lógica del método estaCalificada")
    void testEstaCalificadaLogic() {
        EntregaEvaluacion entregaSinCalificar = new EntregaEvaluacion();
        entregaSinCalificar.setCalificacion(-1.0);
        assertFalse(entregaSinCalificar.estaCalificada());

        EntregaEvaluacion entregaCalificada = new EntregaEvaluacion();
        entregaCalificada.setCalificacion(7.5);
        entregaCalificada.setObservaciones("Aprobado");
        assertTrue(entregaCalificada.estaCalificada());
    }
}