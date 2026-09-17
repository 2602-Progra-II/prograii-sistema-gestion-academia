package com.academia;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaterialDidacticoService {

    public static class MaterialDidactico {
        private String id;
        private String titulo;
        private String descripcion;
        private String urlEnlace;

        public MaterialDidactico(String id, String titulo, String descripcion, String urlEnlace) {
            this.id = id;
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.urlEnlace = urlEnlace;
        }

        public String getId() { return id; }
        public String getTitulo() { return titulo; }
        public String getDescripcion() { return descripcion; }
        public String getUrlEnlace() { return urlEnlace; }
    }

    public static class ActividadEvaluativa {
        private String id;
        private String nombre;
        private double ponderacion;
        private String fechaEntrega;

        public ActividadEvaluativa(String id, String nombre, double ponderacion, String fechaEntrega) {
            this.id = id;
            this.nombre = nombre;
            this.ponderacion = ponderacion;
            this.fechaEntrega = fechaEntrega;
        }

        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public double getPonderacion() { return ponderacion; }
        public String getFechaEntrega() { return fechaEntrega; }
    }

    private final List<MaterialDidactico> materiales = new ArrayList<>();
    private final List<ActividadEvaluativa> actividades = new ArrayList<>();

    public void registrarMaterial(MaterialDidactico material) {
        materiales.add(material);
    }

    public List<MaterialDidactico> obtenerTodosLosMateriales() {
        return materiales;
    }

    public Optional<MaterialDidactico> buscarMaterialPorId(String id) {
        return materiales.stream().filter(m -> m.getId().equals(id)).findFirst();
    }

    public void registrarActividad(ActividadEvaluativa actividad) {
        actividades.add(actividad);
    }

    public List<ActividadEvaluativa> obtenerTodasLasActividades() {
        return actividades;
    }

    public Optional<ActividadEvaluativa> buscarActividadPorId(String id) {
        return actividades.stream().filter(a -> a.getId().equals(id)).findFirst();
    }
}