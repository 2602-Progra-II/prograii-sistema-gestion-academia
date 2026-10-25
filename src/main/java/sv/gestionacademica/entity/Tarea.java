package sv.gestionacademica.entity;

import sv.gestionacademica.enums.TipoActividad;

import java.time.LocalDate;

/** Tarea: se acepta tarde, pero pierde el 20% de la nota. */
public class Tarea extends ActividadEvaluativa {

    private static final double FACTOR_ATRASO = 0.8;

    public Tarea(int idActividad, String nombreActividad, String descripcion,
                 LocalDate fechaLimite, double ponderacion) {
        super(idActividad, nombreActividad, descripcion, fechaLimite, ponderacion);
    }

    @Override
    public double calcularNotaFinal(double notaObtenida) {
        return notaObtenida;
    }

    @Override
    public double calcularNotaFinal(double notaObtenida, LocalDate fechaEntrega) {
        return estaVencidaPara(fechaEntrega) ? notaObtenida * FACTOR_ATRASO : notaObtenida;
    }

    @Override
    public boolean permiteEntregaTardia() {
        return true;
    }

    @Override
    public TipoActividad getTipo() {
        return TipoActividad.TAREA;
    }
}
