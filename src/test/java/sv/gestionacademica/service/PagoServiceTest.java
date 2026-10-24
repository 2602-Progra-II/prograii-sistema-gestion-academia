package sv.gestionacademica.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sv.gestionacademica.entity.Pago;
import sv.gestionacademica.persistence.DataSourceConfig;
import sv.gestionacademica.repository.PagoRepository;
import sv.gestionacademica.repository.PagoRepositoryImpl;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class PagoServiceTest {
    private PagoRepository pagoRepository;
    private PagoService pagoService;

    @BeforeEach
    void setUp() {
        pagoRepository = new PagoRepositoryImpl();
        pagoService = new PagoService(pagoRepository);

        // Limpiar la tabla de pagos antes de cada prueba para mantener un entorno aislado
        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM pago");
        } catch (Exception e) {
            // Silencioso por si la tabla ya está vacía
        }
    }

    @Test
    @DisplayName("Debe registrar un pago correctamente con estado pendiente")
    public void testRegistrarPagoExitoso() {
        Pago pago = new Pago(0, 150.00, LocalDate.now(), "comprobante.pdf", "EST001");
        pagoService.registrarPago(pago, "EST001");

        assertTrue(pago.estaPendiente(), "El pago recién registrado debe estar pendiente");
        assertEquals(150.00, pago.getMonto(), 0.01);
        assertTrue(pago.getIdPago() > 0, "El ID del pago debe ser generado por la BD");
    }

    @Test
    @DisplayName("Debe aprobar un pago existente de forma exitosa")
    public void testAprobacionPagoExitoso() {
        Pago pago = new Pago(0, 200.00, LocalDate.now(), "comprobante2.pdf", "EST002");
        pagoRepository.guardar(pago); // Guarda y asigna el ID real generado

        // Usamos el ID real que la base de datos le otorgó
        pagoService.aprobarPago(pago.getIdPago(), "ASESOR01");

        // Recargamos o validamos el estado actualizado
        Pago pagoActualizado = pagoRepository.buscarPorId(pago.getIdPago()).orElse(null);
        assertNotNull(pagoActualizado);
        assertTrue(pagoActualizado.estaAprobado(), "El pago debería estar aprobado");
        assertEquals("ASESOR01", pagoActualizado.getIdAsesorValidador());
    }

    @Test
    @DisplayName("Debe rechazar un pago existente de forma exitosa")
    public void testRechazoPagoExitoso() {
        Pago pago = new Pago(0, 75.00, LocalDate.now(), "comprobante3.pdf", "EST003");
        pagoRepository.guardar(pago); // Guarda y asigna el ID real generado

        // Usamos el ID real que la base de datos le otorgó
        pagoService.rechazarPago(pago.getIdPago(), "ASESOR01");

        // Recargamos o validamos el estado actualizado
        Pago pagoActualizado = pagoRepository.buscarPorId(pago.getIdPago()).orElse(null);
        assertNotNull(pagoActualizado);
        assertTrue(pagoActualizado.estaRechazado(), "El pago debería estar rechazado");
        assertEquals("ASESOR01", pagoActualizado.getIdAsesorValidador());
    }
}