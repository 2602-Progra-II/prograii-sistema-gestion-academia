package sv.gestionacademica.repository;

import sv.gestionacademica.entity.Factura;

import java.sql.Connection;
import java.util.List;

public interface FacturaRepository {
    void guardar(Factura factura);
    Factura buscarPorId(int idFactura);
    Factura buscarPorNumero(String numeroFactura);
    List<Factura> listarTodas();
    void actualizar(Factura factura);
    void eliminar(int idFactura);

    // Variantes que participan en una transaccion (no cierran la Connection).
    void guardar(Connection con, Factura factura);
    Factura buscarPorPago(Connection con, int idPago);
}
