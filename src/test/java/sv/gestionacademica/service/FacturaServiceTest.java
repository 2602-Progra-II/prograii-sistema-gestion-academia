package sv.gestionacademica.service;
import sv.gestionacademica.entity.Factura;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FacturaServiceTest {

    private FacturaService facturaService;

    @BeforeEach
    void setUp() {
        facturaService = new FacturaService();
    }

    @Test
    @DisplayName("Debe registrar una factura correctamente")
    void testRegistrarFacturaExitoso() {

        Factura factura = new Factura(
                1,
                "FAC-001",
                LocalDate.of(2026, 9, 16),
                150.00
        );

        boolean registrada = facturaService.registrarFactura(factura);

        assertTrue(registrada, "La factura debería registrarse correctamente");

        Factura resultado = facturaService.obtenerFactura(1);

        assertNotNull(resultado, "La factura debería existir después de registrarla");
        assertEquals("FAC-001", resultado.getNumeroFactura());
        assertEquals(150.00, resultado.getMontoTotal());
    }

    @Test
    @DisplayName("No debe permitir registrar una factura duplicada")
    void testRegistrarFacturaDuplicada() {

        Factura factura1 = new Factura(
                1,
                "FAC-001",
                LocalDate.of(2026, 9, 16),
                150.00
        );

        Factura factura2 = new Factura(
                1,
                "FAC-002",
                LocalDate.of(2026, 9, 16),
                200.00
        );

        assertTrue(facturaService.registrarFactura(factura1));

        assertFalse(
                facturaService.registrarFactura(factura2),
                "No debería permitir dos facturas con el mismo ID"
        );
    }

    @Test
    @DisplayName("Debe lanzar excepción si el número de factura está vacío")
    void testNumeroFacturaInvalido() {

        Factura factura = new Factura(
                2,
                "",
                LocalDate.of(2026, 9, 16),
                100.00
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> facturaService.registrarFactura(factura),
                "Debe lanzar excepción si el número de factura está vacío"
        );
    }

    @Test
    @DisplayName("Debe eliminar una factura correctamente")
    void testEliminarFactura() {

        Factura factura = new Factura(
                3,
                "FAC-003",
                LocalDate.of(2026, 9, 16),
                250.00
        );

        facturaService.registrarFactura(factura);

        boolean eliminado = facturaService.eliminarFactura(3);

        assertTrue(eliminado, "La factura debería eliminarse correctamente");

        assertNull(
                facturaService.obtenerFactura(3),
                "La factura ya no debería existir"
        );
    }

    @Test
    @DisplayName("Debe obtener correctamente la cantidad de facturas")
    void testCantidadFacturas() {

        Factura factura1 = new Factura(
                1,
                "FAC-001",
                LocalDate.of(2026, 9, 16),
                100.00
        );

        Factura factura2 = new Factura(
                2,
                "FAC-002",
                LocalDate.of(2026, 9, 16),
                200.00
        );

        facturaService.registrarFactura(factura1);
        facturaService.registrarFactura(factura2);

        assertEquals(
                2,
                facturaService.obtenerCantidadFacturas()
        );
    }
}
