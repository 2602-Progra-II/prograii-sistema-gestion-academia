package sv.gestionacademica.repository;

import sv.gestionacademica.entity.Pago;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PagoRepository {
    private final List<Pago> pagos = new ArrayList<>();

    public void guardar(Pago pago) {
        if (pago == null) {
            throw new IllegalArgumentException("El pago no puede ser nulo.");
        }
        pagos.add(pago);
    }

    public Optional<Pago> buscarPorId(int id) {
        return pagos.stream().filter(p -> p.getIdPago() == id).findFirst();
    }

    public List<Pago> listarPendientes() {
        List<Pago> pendientes = new ArrayList<>();
        for (Pago p : pagos) {
            if (p.estaPendiente()) {
                pendientes.add(p);
            }
        }
        return pendientes;
    }

    public void actualizar(Pago pagoActualizado) {
        for (int i = 0; i < pagos.size(); i++) {
            if (pagos.get(i).getIdPago() == pagoActualizado.getIdPago()) {
                pagos.set(i, pagoActualizado);
                return;
            }
        }
    }
}
