package sv.gestionacademica.repository;

import sv.gestionacademica.entity.Factura;
import java.util.List;

public interface FacturaRepository {
    void guardar(Factura factura);
    Factura buscarPorId(int idFactura);
    Factura buscarPorNumero(String numeroFactura);
    List<Factura> listarTodas();
    void actualizar(Factura factura);
    void eliminar(int idFactura);
}