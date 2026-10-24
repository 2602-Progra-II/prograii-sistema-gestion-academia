package sv.gestionacademica.repository;

import sv.gestionacademica.entity.Pago;
import java.util.List;
import java.util.Optional;

public interface PagoRepository {
    void guardar(Pago pago);
    Optional<Pago> buscarPorId(int id);
    List<Pago> listarPendientes();
    void actualizar(Pago pagoActualizado);
}