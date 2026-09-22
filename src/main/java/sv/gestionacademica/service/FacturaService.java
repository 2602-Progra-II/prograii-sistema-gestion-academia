package sv.gestionacademica.service;

import sv.gestionacademica.entity.Factura;
import sv.gestionacademica.repository.FacturaRepository;

public class FacturaService {

    private FacturaRepository facturaRepository;

    public FacturaService() {
        facturaRepository = new FacturaRepository();
    }

    public boolean registrarFactura(Factura factura) {

        if (factura == null) {
            throw new IllegalArgumentException("La factura no puede ser nula");
        }

        if (factura.getNumeroFactura() == null ||
                factura.getNumeroFactura().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El número de factura no puede estar vacío"
            );
        }

        if (factura.getFechaEmision() == null) {
            throw new IllegalArgumentException(
                    "La fecha de emisión no puede ser nula"
            );
        }

        if (factura.getMontoTotal() < 0) {
            throw new IllegalArgumentException(
                    "El monto total no puede ser negativo"
            );
        }

        return facturaRepository.guardar(factura);
    }

    public Factura obtenerFactura(int idFactura) {
        return facturaRepository.buscarPorId(idFactura);
    }

    public boolean eliminarFactura(int idFactura) {
        return facturaRepository.eliminar(idFactura);
    }

    public int obtenerCantidadFacturas() {
        return facturaRepository.obtenerTotalFacturas();
    }

    public void limpiarFacturas() {
        facturaRepository.limpiar();
    }
}
