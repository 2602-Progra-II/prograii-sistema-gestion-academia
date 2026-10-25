package sv.gestionacademica.entity;

import sv.gestionacademica.enums.TipoActividad;

import java.time.LocalDate;

/** Proyecto: no admite entregas tardias; entregarlo a tiempo da 5% de bonificacion (maximo 10). */
public class Proyecto extends ActividadEvaluativa {

    private static final double FACTOR_BONIFICACION = 1.05;
    private static final double NOTA_MAXIMA = 10.0;

    public Proyecto(int idActividad, String nombreActividad, String descripcion,
                    LocalDate fechaLimite, double ponderacion) {
        super(idActividad, nombreActividad, descripcion, fechaLimite, ponderacion);
    }

    @Override
    public double calcularNotaFinal(double notaObtenida) {
        return Math.min(notaObtenida * FACTOR_BONIFICACION, NOTA_MAXIMA);
    }

    @Override
    public double calcularNotaFinal(double notaObtenida, LocalDate fechaEntrega) {
        return estaVencidaPara(fechaEntrega) ? notaObtenida : calcularNotaFinal(notaObtenida);
    }

    @Override
    public TipoActividad getTipo() {
        return TipoActividad.PROYECTO;
    }
}
