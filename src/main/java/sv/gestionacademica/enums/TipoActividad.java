package sv.gestionacademica.enums;

import sv.gestionacademica.entity.ActividadEvaluativa;
import sv.gestionacademica.entity.Examen;
import sv.gestionacademica.entity.Proyecto;
import sv.gestionacademica.entity.Tarea;

import java.time.LocalDate;

/**
 * Tipo de actividad evaluativa (columna actividad_evaluativa.tipo_actividad).
 * Cada constante sabe construir su subclase: asi el repositorio y el controller
 * crean la especializacion correcta sin un switch/if por tipo (fabrica polimorfica).
 */
public enum TipoActividad {

    TAREA {
        @Override
        public ActividadEvaluativa crear(int id, String nombre, String descripcion, LocalDate limite, double ponderacion) {
            return new Tarea(id, nombre, descripcion, limite, ponderacion);
        }
    },
    EXAMEN {
        @Override
        public ActividadEvaluativa crear(int id, String nombre, String descripcion, LocalDate limite, double ponderacion) {
            return new Examen(id, nombre, descripcion, limite, ponderacion);
        }
    },
    PROYECTO {
        @Override
        public ActividadEvaluativa crear(int id, String nombre, String descripcion, LocalDate limite, double ponderacion) {
            return new Proyecto(id, nombre, descripcion, limite, ponderacion);
        }
    };

    public abstract ActividadEvaluativa crear(int id, String nombre, String descripcion,
                                              LocalDate limite, double ponderacion);

    /** Convierte el texto de la BD; si viene vacio o desconocido se trata como TAREA. */
    public static TipoActividad desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return TAREA;
        }
        try {
            return valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return TAREA;
        }
    }
}
