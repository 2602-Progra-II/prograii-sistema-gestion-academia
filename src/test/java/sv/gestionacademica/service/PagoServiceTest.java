package sv.gestionacademica.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sv.gestionacademica.entity.Pago;
import sv.gestionacademica.repository.PagoRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class PagoServiceTest {
    private PagoRepository pagoRepository;
    private PagoService pagoService;

    @BeforeEach
    public void setUp() {
        pagoRepository = new PagoRepository();
        pagoService = new PagoService(pagoRepository);
    }

    @Test
    public void testRegistrarPagoExitoso() {
        Pago pago = new Pago(1, 150.00, LocalDate.now(), "comprobante.pdf", "EST001");
        pagoService.registrarPago(pago, "EST001");

        assertTrue(pago.estaPendiente());
        assertEquals(150.00, pago.getMonto());
    }

    @Test
    public void testAprobacionPagoExitoso() {
        Pago pago = new Pago(2, 200.00, LocalDate.now(), "comprobante2.pdf", "EST002");
        pagoRepository.guardar(pago);

        pagoService.aprobarPago(2, "ASESOR01");

        assertTrue(pago.estaAprobado());
        assertEquals("ASESOR01", pago.getIdAsesorValidador());
    }

    @Test
    public void testRechazoPagoExitoso() {
        Pago pago = new Pago(3, 75.00, LocalDate.now(), "comprobante3.pdf", "EST003");
        pagoRepository.guardar(pago);

        pagoService.rechazarPago(3, "ASESOR01");

        assertTrue(pago.estaRechazado());
        assertEquals("ASESOR01", pago.getIdAsesorValidador());
    }
}
