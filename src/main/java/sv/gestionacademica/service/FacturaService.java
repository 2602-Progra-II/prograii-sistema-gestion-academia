package sv.gestionacademica.service;

import sv.gestionacademica.entity.Factura;
import sv.gestionacademica.repository.FacturaRepository;
import sv.gestionacademica.repository.FacturaRepositoryImpl;

import java.util.List;

public class FacturaService {

    private FacturaRepository facturaRepository;

    public FacturaService() {
        this.facturaRepository = new FacturaRepositoryImpl();
    }

    public boolean registrarFactura(Factura factura) {
        if (factura == null) {
            throw new IllegalArgumentException("La factura no puede ser nula");
        }

        if (factura.getNumeroFactura() == null || factura.getNumeroFactura().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de factura no puede estar vacío");
        }

        if (factura.getFechaEmision() == null) {
            throw new IllegalArgumentException("La fecha de emisión no puede ser nula");
        }

        if (factura.getMontoTotal() < 0) {
            throw new IllegalArgumentException("El monto total no puede ser negativo");
        }

        // CORRECCIÓN: Validar si ya existe una factura con ese mismo número único
        if (facturaRepository.buscarPorNumero(factura.getNumeroFactura()) != null) {
            return false; // Retorna falso si ya existe, evitando el error de SQL
        }

        facturaRepository.guardar(factura);
        return true;
    }

    public Factura obtenerFactura(int idFactura) {
        return facturaRepository.buscarPorId(idFactura);
    }

    public boolean eliminarFactura(int idFactura) {
        Factura existente = facturaRepository.buscarPorId(idFactura);
        if (existente == null) {
            return false;
        }
        facturaRepository.eliminar(idFactura);
        return true;
    }

    public int obtenerCantidadFacturas() {
        List<Factura> facturas = facturaRepository.listarTodas();
        return facturas != null ? facturas.size() : 0;
    }
}