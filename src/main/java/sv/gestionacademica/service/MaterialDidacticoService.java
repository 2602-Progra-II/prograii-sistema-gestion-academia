package sv.gestionacademica.service;

import sv.gestionacademica.entity.MaterialDidactico;
import sv.gestionacademica.entity.ActividadEvaluativa;

import java.util.List;
import java.util.Optional;

public class MaterialDidacticoService {

    private final MaterialService materialService;
    private final ActividadService actividadService;

    public MaterialDidacticoService() {
        this.materialService = new MaterialService();
        this.actividadService = new ActividadService();
    }

    public MaterialDidacticoService(MaterialService materialService, ActividadService actividadService) {
        this.materialService = materialService;
        this.actividadService = actividadService;
    }

    public void registrarMaterial(MaterialDidactico material) {
        materialService.registrarMaterial(material);
    }

    public List<MaterialDidactico> obtenerTodosLosMateriales() {
        return materialService.obtenerTodosLosMateriales();
    }

    public Optional<MaterialDidactico> buscarMaterialPorId(String id) {
        return materialService.buscarMaterialPorId(id);
    }

    // Métodos para ActividadEvaluativa
    public void registrarActividad(ActividadEvaluativa actividad) {
        actividadService.registrarActividad(actividad);
    }

    public List<ActividadEvaluativa> obtenerTodasLasActividades() {
        return actividadService.obtenerTodasLasActividades();
    }

    public Optional<ActividadEvaluativa> buscarActividadPorId(String id) {
        return actividadService.buscarActividadPorId(id);
    }
}