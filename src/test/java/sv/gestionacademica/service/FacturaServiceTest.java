package sv.gestionacademica.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sv.gestionacademica.entity.Factura;
import sv.gestionacademica.persistence.DataSourceConfig;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FacturaServiceTest {

    private FacturaService facturaService;

    @BeforeEach
    void setUp() {
        facturaService = new FacturaService();


        try (var conn = DataSourceConfig.getDataSource().getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM factura");
        } catch (Exception e) {

        }
    }

    @Test
    @DisplayName("Debe registrar una factura correctamente")
    void testRegistrarFacturaExitoso() {
        Factura factura = new Factura(0, "FAC-001", LocalDate.of(2026, 9, 16), 150.00);

        boolean resultado = facturaService.registrarFactura(factura);

        assertTrue(resultado, "La factura debería registrarse correctamente");
        assertTrue(factura.getIdFactura() > 0, "El ID autogenerado por la BD debería ser mayor a 0");

        Factura encontrada = facturaService.obtenerFactura(factura.getIdFactura());
        assertNotNull(encontrada, "La factura debería existir después de registrarla");
    }

    @Test
    @DisplayName("No debe permitir registrar dos facturas con el mismo número único")
    void testRegistrarFacturaDuplicada() {
        Factura factura1 = new Factura(0, "FAC-DUP-01", LocalDate.now(), 100.00);
        Factura factura2 = new Factura(0, "FAC-DUP-01", LocalDate.now(), 200.00);

        boolean resultado1 = facturaService.registrarFactura(factura1);
        boolean resultado2 = facturaService.registrarFactura(factura2); // Debería fallar por número duplicado

        assertTrue(resultado1, "La primera factura debe registrarse");
        assertFalse(resultado2, "No debería permitir registrar un número de factura duplicado");
    }

    @Test
    @DisplayName("Debe obtener correctamente la cantidad de facturas")
    void testCantidadFacturas() {
        Factura factura1 = new Factura(0, "FAC-100", LocalDate.now(), 50.00);
        Factura factura2 = new Factura(0, "FAC-200", LocalDate.now(), 75.00);

        facturaService.registrarFactura(factura1);
        facturaService.registrarFactura(factura2);

        int cantidad = facturaService.obtenerCantidadFacturas();
        assertEquals(2, cantidad, "Debería haber exactamente 2 facturas registradas");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el número de factura está vacío o es nulo")
    void testFacturaInvalida() {
        Factura facturaNula = new Factura(0, "", LocalDate.now(), 50.00);

        assertThrows(IllegalArgumentException.class, () -> {
            facturaService.registrarFactura(facturaNula);
        }, "Debería lanzar IllegalArgumentException si el número de factura está vacío");
    }

    @Test
    @DisplayName("Debe eliminar una factura correctamente")
    void testEliminarFactura() {
        Factura factura = new Factura(0, "FAC-DEL-01", LocalDate.now(), 300.00);
        facturaService.registrarFactura(factura);

        // Eliminamos usando el ID real que la base de datos le asignó automáticamente
        boolean eliminado = facturaService.eliminarFactura(factura.getIdFactura());

        assertTrue(eliminado, "La factura debería eliminarse correctamente");
        assertNull(facturaService.obtenerFactura(factura.getIdFactura()), "La factura ya no debería existir en la BD");
    }
}