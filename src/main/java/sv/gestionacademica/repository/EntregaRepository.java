package sv.gestionacademica.repository;

import sv.gestionacademica.entity.EntregaEvaluacion;
import java.util.List;
import java.util.Optional;

public interface EntregaRepository {

    EntregaEvaluacion guardar(EntregaEvaluacion entrega);

    Optional<EntregaEvaluacion> buscarPorId(int idEntrega);

    List<EntregaEvaluacion> listarPorActividad(int idActividad);

    List<EntregaEvaluacion> listarPorEstudiante(String idEstudiante);

    List<EntregaEvaluacion> listarTodas();
}
