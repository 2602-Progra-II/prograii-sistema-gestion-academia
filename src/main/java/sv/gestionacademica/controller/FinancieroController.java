package sv.gestionacademica.controller;

import sv.gestionacademica.entity.Pago;
import sv.gestionacademica.service.PagoService;
import java.time.LocalDate;

public class FinancieroController {
    private final PagoService pagoService;

    public FinancieroController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    public void registrarPago(int idPago, double monto, String rutaComprobante, String idEstudiante) {
        try {
            Pago nuevoPago = new Pago(idPago, monto, LocalDate.now(), rutaComprobante, idEstudiante);
            pagoService.registrarPago(nuevoPago, idEstudiante);
            System.out.println("Pago registrado exitosamente para el estudiante: " + idEstudiante);
        } catch (Exception e) {
            System.err.println("Error al registrar el pago: " + e.getMessage());
        }
    }

    public void aprobarPago(int idPago, String asesorId) {
        try {
            pagoService.aprobarPago(idPago, asesorId);
            System.out.println("El pago ID " + idPago + " fue aprobado por " + asesorId);
        } catch (Exception e) {
            System.err.println("Error al aprobar el pago: " + e.getMessage());
        }
    }

    public void rechazarPago(int idPago, String asesorId) {
        try {
            pagoService.rechazarPago(idPago, asesorId);
            System.err.println("El pago ID " + idPago + " fue rechazado por " + asesorId);
        } catch (Exception e) {
            System.err.println("Error al rechazar el pago: " + e.getMessage());
        }
    }
}
