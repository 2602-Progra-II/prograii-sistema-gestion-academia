package sv.gestionacademica.repository;

import sv.gestionacademica.entity.Factura;

import java.util.HashMap;
import java.util.Map;

public class FacturaRepository {

    private Map<Integer, Factura> facturas = new HashMap<>();

    public boolean guardar(Factura factura) {
        if (factura == null) {
            throw new IllegalArgumentException("La factura no puede ser nula");
        }

        if (facturas.containsKey(factura.getIdFactura())) {
            return false;
        }

        facturas.put(factura.getIdFactura(), factura);
        return true;
    }

    public Factura buscarPorId(int idFactura) {
        return facturas.get(idFactura);
    }

    public boolean eliminar(int idFactura) {
        if (facturas.containsKey(idFactura)) {
            facturas.remove(idFactura);
            return true;
        }

        return false;
    }

    public int obtenerTotalFacturas() {
        return facturas.size();
    }

    public void limpiar() {
        facturas.clear();
    }
}
