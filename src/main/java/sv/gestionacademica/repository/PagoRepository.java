package sv.gestionacademica.repository;

import sv.gestionacademica.entity.Pago;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface PagoRepository {
    void guardar(Pago pago);
    Optional<Pago> buscarPorId(int id);
    List<Pago> listarPendientes();
    /** Todos los pagos de un estudiante (cualquier estado). Lo usa el estado de cuenta. */
    List<Pago> listarPorEstudiante(String idEstudiante);
    void actualizar(Pago pagoActualizado);

    // Variantes que participan en una transaccion (no cierran la Connection).
    void guardar(Connection con, Pago pago);
    Optional<Pago> buscarPorId(Connection con, int id);
    /** Igual que buscarPorId pero bloquea la fila hasta el commit/rollback (evita aprobar dos veces a la vez). */
    Optional<Pago> buscarPorIdBloqueando(Connection con, int id);
    void actualizar(Connection con, Pago pagoActualizado);
}
