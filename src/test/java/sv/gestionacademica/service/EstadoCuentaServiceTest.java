package sv.gestionacademica.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EstadoCuentaServiceTest {

    private EstadoCuentaService service;

    @BeforeEach
    void setUp() {
        service = new EstadoCuentaService();
    }

    @Test
    void testRegistrarYConsultarEstadoCuenta() {
        EstadoCuenta cuenta = new EstadoCuenta("2026011706", "Bryan Reyes", 150.0, true);
        service.registrarEstadoCuenta(cuenta);

        EstadoCuenta consultada = service.consultarEstadoCuenta("2026011706");
        assertNotNull(consultada);
        assertEquals("Bryan Reyes", consultada.getNombreEstudiante());
        assertEquals(150.0, consultada.getSaldoPendiente());
        assertTrue(consultada.isEnMora());
    }

    @Test
    void testObtenerEstudiantesEnMora() {
        EstadoCuenta cuenta1 = new EstadoCuenta("001", "Estudiante Uno", 0.0, false);
        EstadoCuenta cuenta2 = new EstadoCuenta("002", "Estudiante Dos", 200.0, true);

        service.registrarEstadoCuenta(cuenta1);
        service.registrarEstadoCuenta(cuenta2);

        assertEquals(1, service.obtenerEstudiantesEnMora().size());
        assertTrue(service.obtenerEstudiantesEnMora().get(0).isEnMora());
    }
}