package com.academia.pagos;

public class PagoService {
    private final PagoRepository pagoRepository;

    public PagoService(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    public void registrarPago(Pago pago, String estudianteId) {
        if (pago == null || estudianteId == null || estudianteId.isEmpty()) {
            throw new IllegalArgumentException("Datos de pago o estudiante inválidos.");
        }
        if (pago.getMonto() <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero.");
        }
        pago.setIdEstudiante(estudianteId);
        pago.setEstadoPago(EstadoPago.PENDIENTE);
        pagoRepository.guardar(pago);
    }

    public void aprobarPago(int idPago, String asesorId) {
        Pago pago = pagoRepository.buscarPorId(idPago)
                .orElseThrow(() -> new IllegalStateException("Pago no encontrado."));

        if (!pago.estaPendiente()) {
            throw new IllegalStateException("El pago ya ha sido procesado anteriormente.");
        }

        pago.setEstadoPago(EstadoPago.APROBADO);
        pago.setIdAsesorValidador(asesorId);
        pagoRepository.actualizar(pago);
    }

    public void rechazarPago(int idPago, String asesorId) {
        Pago pago = pagoRepository.buscarPorId(idPago)
                .orElseThrow(() -> new IllegalStateException("Pago no encontrado."));

        if (!pago.estaPendiente()) {
            throw new IllegalStateException("El pago ya ha sido procesado anteriormente.");
        }

        pago.setEstadoPago(EstadoPago.RECHAZADO);
        pago.setIdAsesorValidador(asesorId);
        pagoRepository.actualizar(pago);
    }
}
