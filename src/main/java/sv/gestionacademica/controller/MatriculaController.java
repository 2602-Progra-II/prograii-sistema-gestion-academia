package sv.gestionacademica.controller;

import sv.gestionacademica.dto.ResultadoMatricula;
import sv.gestionacademica.entity.Factura;
import sv.gestionacademica.exception.BusinessException;
import sv.gestionacademica.exception.TransaccionException;
import sv.gestionacademica.service.FlujoMatriculaService;

import java.util.Optional;

/**
 * Entrada del flujo transaccional core (matricula -> pago -> factura).
 * Traduce las excepciones del servicio a mensajes: reglas de negocio vs errores tecnicos.
 */
public class MatriculaController {

    private final FlujoMatriculaService flujoService;

    public MatriculaController(FlujoMatriculaService flujoService) {
        this.flujoService = flujoService;
    }

    public Optional<ResultadoMatricula> matricularEstudiante(int idEstudiante, int idCurso, double monto) {
        try {
            ResultadoMatricula r = flujoService.matricular(idEstudiante, idCurso, monto);
            System.out.println("Matrícula " + r.matricula().getIdMatricula()
                    + " creada (" + r.matricula().getEstadoMatricula() + "). Pago pendiente id "
                    + r.pago().getIdPago() + " por $" + r.pago().getMonto());
            return Optional.of(r);
        } catch (BusinessException e) {
            System.err.println("[Regla de negocio] " + e.getMessage());
        } catch (TransaccionException e) {
            System.err.println("[Error técnico] " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Factura> aprobarPago(int idPago, String idAsesor) {
        try {
            Factura f = flujoService.aprobarPago(idPago, idAsesor);
            System.out.println("Pago " + idPago + " aprobado. Matrícula activada. Factura "
                    + f.getNumeroFactura() + " por $" + f.getMontoTotal());
            return Optional.of(f);
        } catch (BusinessException e) {
            System.err.println("[Regla de negocio] " + e.getMessage());
        } catch (TransaccionException e) {
            System.err.println("[Error técnico] " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean rechazarPago(int idPago, String idAsesor) {
        try {
            flujoService.rechazarPago(idPago, idAsesor);
            System.out.println("Pago " + idPago + " rechazado. Matrícula cancelada.");
            return true;
        } catch (BusinessException e) {
            System.err.println("[Regla de negocio] " + e.getMessage());
        } catch (TransaccionException e) {
            System.err.println("[Error técnico] " + e.getMessage());
        }
        return false;
    }
}
