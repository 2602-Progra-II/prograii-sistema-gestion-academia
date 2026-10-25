package sv.gestionacademica.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sv.gestionacademica.entity.PeriodoAcademico;
import sv.gestionacademica.enums.EstadoPeriodo;
import sv.gestionacademica.persistence.DataSourceConfig;
import sv.gestionacademica.repository.PeriodoAcademicoRepository;
import sv.gestionacademica.repository.PeriodoAcademicoRepositoryImpl;
import sv.gestionacademica.service.PeriodoAcademicoService;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class PeriodoAcademicoServiceTest {

    private PeriodoAcademicoService periodoAcademicoService;
    private PeriodoAcademicoRepository repository;

    @BeforeEach
    void setUp() {
        repository = new PeriodoAcademicoRepositoryImpl();
        periodoAcademicoService = new PeriodoAcademicoService(repository);

        // Limpieza de tablas dependientes en SQL Server
        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {

            String[] tablas = {"pago", "factura", "entrega", "matricula", "actividad", "material_didactico", "curso", "periodo_academico"};
            for (String tabla : tablas) {
                try {
                    stmt.executeUpdate("DELETE FROM " + tabla);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            // Silencioso por si las tablas están vacías
        }
    }

    @Test
    @DisplayName("Debería registrar un periodo académico correctamente en la BD")
    void testRegistrarPeriodoExitoso() {
        String nombreUnico = "Ciclo 01-" + System.currentTimeMillis();
        PeriodoAcademico periodo = new PeriodoAcademico(0, nombreUnico, LocalDate.now(), LocalDate.now().plusMonths(4), EstadoPeriodo.PLANIFICADO);

        PeriodoAcademico resultado = periodoAcademicoService.registrarPeriodo(periodo);

        assertNotNull(resultado);
        assertTrue(resultado.getIdPeriodo() > 0, "El ID del periodo debe ser generado por la BD");
        assertEquals(nombreUnico, resultado.getNombrePeriodo());
    }

    @Test
    @DisplayName("Debería lanzar excepción al registrar periodo con fechas inválidas")
    void testRegistrarPeriodoFechasInvalidas() {
        PeriodoAcademico periodo = new PeriodoAcademico(0, "Ciclo Inv-" + System.currentTimeMillis(), LocalDate.now().plusMonths(4), LocalDate.now(), EstadoPeriodo.PLANIFICADO);

        assertThrows(IllegalArgumentException.class, () -> {
            periodoAcademicoService.registrarPeriodo(periodo);
        });
    }

    @Test
    @DisplayName("Debería lanzar excepción al registrar un periodo con nombre duplicado")
    void testRegistrarPeriodoNombreDuplicado() {
        String nombreDuplicado = "Ciclo Dup-" + System.currentTimeMillis();
        PeriodoAcademico periodo1 = new PeriodoAcademico(0, nombreDuplicado, LocalDate.now(), LocalDate.now().plusMonths(4), EstadoPeriodo.PLANIFICADO);
        periodoAcademicoService.registrarPeriodo(periodo1);

        PeriodoAcademico periodo2 = new PeriodoAcademico(0, nombreDuplicado, LocalDate.now(), LocalDate.now().plusMonths(4), EstadoPeriodo.PLANIFICADO);

        assertThrows(IllegalArgumentException.class, () -> {
            periodoAcademicoService.registrarPeriodo(periodo2);
        });
    }

    @Test
    @DisplayName("Debería modificar el nombre de un periodo existente en la BD")
    void testModificarPeriodoExitoso() {
        String nombreOriginal = "Ciclo Orig-" + System.currentTimeMillis();
        PeriodoAcademico periodo = new PeriodoAcademico(0, nombreOriginal, LocalDate.now(), LocalDate.now().plusMonths(4), EstadoPeriodo.PLANIFICADO);
        PeriodoAcademico registrado = periodoAcademicoService.registrarPeriodo(periodo);

        String nuevoNombre = "Ciclo Mod-" + System.currentTimeMillis();
        PeriodoAcademico modificado = periodoAcademicoService.modificarPeriodo(registrado.getIdPeriodo(), nuevoNombre);

        assertEquals(nuevoNombre, modificado.getNombrePeriodo());
    }

    @Test
    @DisplayName("Debería activar un periodo existente en la BD")
    void testActivarPeriodoExitoso() {
        String nombreAct = "Ciclo Act-" + System.currentTimeMillis();
        PeriodoAcademico periodo = new PeriodoAcademico(0, nombreAct, LocalDate.now(), LocalDate.now().plusMonths(4), EstadoPeriodo.PLANIFICADO);
        PeriodoAcademico registrado = periodoAcademicoService.registrarPeriodo(periodo);

        PeriodoAcademico activado = periodoAcademicoService.activarPeriodo(registrado.getIdPeriodo());

        assertEquals(EstadoPeriodo.ACTIVO, activado.getEstado());
    }

    @Test
    @DisplayName("Debería finalizar un periodo existente en la BD")
    void testFinalizarPeriodoExitoso() {
        String nombreFin = "Ciclo Fin-" + System.currentTimeMillis();
        PeriodoAcademico periodo = new PeriodoAcademico(0, nombreFin, LocalDate.now(), LocalDate.now().plusMonths(4), EstadoPeriodo.PLANIFICADO);
        PeriodoAcademico registrado = periodoAcademicoService.registrarPeriodo(periodo);
        periodoAcademicoService.activarPeriodo(registrado.getIdPeriodo());

        PeriodoAcademico finalizado = periodoAcademicoService.finalizarPeriodo(registrado.getIdPeriodo());

        assertEquals(EstadoPeriodo.FINALIZADO, finalizado.getEstado());
    }

    @Test
    @DisplayName("Debería lanzar NoSuchElementException al consultar un periodo inexistente")
    void testConsultarPeriodoInexistente() {
        assertThrows(NoSuchElementException.class, () -> {
            periodoAcademicoService.consultarPeriodo(999999);
        });
    }

    @Test
    @DisplayName("Debería listar todos los periodos registrados desde la BD")
    void testListarPeriodos() {
        long t = System.currentTimeMillis();
        PeriodoAcademico p1 = new PeriodoAcademico(0, "Ciclo L1-" + t, LocalDate.now(), LocalDate.now().plusMonths(4), EstadoPeriodo.PLANIFICADO);
        PeriodoAcademico p2 = new PeriodoAcademico(0, "Ciclo L2-" + t, LocalDate.now(), LocalDate.now().plusMonths(4), EstadoPeriodo.PLANIFICADO);

        periodoAcademicoService.registrarPeriodo(p1);
        periodoAcademicoService.registrarPeriodo(p2);

        List<PeriodoAcademico> lista = periodoAcademicoService.listarPeriodos();

        assertEquals(2, lista.size());
    }
}