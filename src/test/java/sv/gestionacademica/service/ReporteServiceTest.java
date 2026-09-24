package sv.gestionacademica.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import sv.gestionacademica.entity.IndicadorMora;

class ReporteServiceTest {

    private ReporteService reporteService;

    @BeforeEach
    void setUp() {
        reporteService = new ReporteService();
    }

    @Test
    void testGenerarIndicadorMora() {
        IndicadorMora indicador = reporteService.generarIndicadorMora(5, 1250.75, "Ciclo 02-2026");

        assertNotNull(indicador);
        assertEquals(5, indicador.getTotalEstudiantesEnMora());
        assertEquals(1250.75, indicador.getMontoTotalPendiente());
        assertEquals("Ciclo 02-2026", indicador.getPeriodoAcademico());
    }

    @Test
    void testCalcularRendimientoCurso() {

        assertDoesNotThrow(() -> {
            reporteService.calcularRendimientoCurso("Programación II", 8.5, 30, 5);
        });
    }
}