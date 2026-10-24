package sv.gestionacademica.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sv.gestionacademica.entity.ActividadEvaluativa;
import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.entity.PeriodoAcademico;
import sv.gestionacademica.enums.EstadoCurso;
import sv.gestionacademica.enums.EstadoPeriodo;
import sv.gestionacademica.exception.ActividadInvalidaException;
import sv.gestionacademica.exception.ActividadNoEncontradaException;
import sv.gestionacademica.persistence.DataSourceConfig;
import sv.gestionacademica.repository.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ActividadServiceTest {

    private ActividadService actividadService;
    private ActividadRepository actividadRepository;

    @BeforeEach
    void setUp() {
        actividadRepository = new ActividadRepositoryImpl();
        actividadService = new ActividadService(actividadRepository);

        // Limpieza ordenada de tablas
        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {

            String[] tablas = {"entrega_evaluacion", "actividad_evaluativa", "matricula", "curso", "periodo_academico"};
            for (String tabla : tablas) {
                try {
                    stmt.executeUpdate("DELETE FROM " + tabla);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}

        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 5);

        // Periodo y Curso requeridos para la llave foránea
        PeriodoAcademicoRepository periodoRepo = new PeriodoAcademicoRepositoryImpl();
        PeriodoAcademico periodo = new PeriodoAcademico(0, "P_" + suffix, LocalDate.now(), LocalDate.now().plusMonths(4), EstadoPeriodo.ACTIVO);
        periodoRepo.guardar(periodo);

        CursoRepository cursoRepo = new CursoRepositoryImpl();
        Curso curso = new Curso(0, "C_" + suffix, "Curso Test", "Descripcion", EstadoCurso.ACTIVO, periodo.getIdPeriodo(), null);
        cursoRepo.guardar(curso);
    }

    @Test
    @DisplayName("Debería registrar una actividad evaluativa en la BD")
    void testRegistrarActividadExitoso() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 5);
        ActividadEvaluativa act = crearActividad("0", "Examen Parcial " + suffix, "Evaluación individual", 35.0);

        ActividadEvaluativa registrada = actividadService.registrarActividad(act);

        assertNotNull(registrada);
        assertNotNull(registrada.getId());
        assertFalse(registrada.getId().isEmpty());
    }

    @Test
    @DisplayName("Debería lanzar ActividadInvalidaException si la ponderación es inválida")
    void testRegistrarActividadPonderacionInvalida() {
        ActividadEvaluativa act = crearActividad("0", "Examen Final", "Evaluación", 150.0);

        assertThrows(ActividadInvalidaException.class, () -> actividadService.registrarActividad(act));
    }

    @Test
    @DisplayName("Debería obtener todas las actividades desde la BD")
    void testObtenerTodasLasActividades() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 5);
        ActividadEvaluativa a1 = crearActividad("0", "Lab 1 " + suffix, "Práctica", 15.0);
        ActividadEvaluativa a2 = crearActividad("0", "Lab 2 " + suffix, "Práctica", 15.0);

        actividadService.registrarActividad(a1);
        actividadService.registrarActividad(a2);

        List<ActividadEvaluativa> lista = actividadService.obtenerTodasLasActividades();

        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("Debería eliminar una actividad evaluativa en la BD")
    void testEliminarActividadExitoso() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 5);
        ActividadEvaluativa act = crearActividad("0", "Quiz " + suffix, "Prueba corta", 10.0);
        ActividadEvaluativa creada = actividadService.registrarActividad(act);

        boolean eliminada = actividadService.eliminarActividad(creada.getId());

        assertTrue(eliminada);
        assertThrows(ActividadNoEncontradaException.class, () -> actividadService.obtenerActividadPorId(creada.getId()));
    }

    private ActividadEvaluativa crearActividad(String id, String nombre, String descripcion, double ponderacion) {
        return new ActividadEvaluativa(0, nombre, descripcion, LocalDate.now().plusDays(5), ponderacion) {
            @Override
            public double calcularNotaFinal(double notaObtenida) {
                return (notaObtenida * ponderacion) / 100.0;
            }
        };
    }
}