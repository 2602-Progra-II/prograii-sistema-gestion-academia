package sv.gestionacademica.entity;

import sv.gestionacademica.enums.TipoActividad;

import java.time.LocalDate;

/** Examen: no admite entregas tardias; la nota se reporta tal cual y se aprueba con 6.0. */
public class Examen extends ActividadEvaluativa {

    private static final double NOTA_MINIMA = 6.0;

    public Examen(int idActividad, String nombreActividad, String descripcion,
                  LocalDate fechaLimite, double ponderacion) {
        super(idActividad, nombreActividad, descripcion, fechaLimite, ponderacion);
    }

    @Override
    public double calcularNotaFinal(double notaObtenida) {
        return Math.max(notaObtenida, 0.0);
    }

    public boolean estaAprobado(double notaObtenida) {
        return notaObtenida >= NOTA_MINIMA;
    }

    @Override
    public TipoActividad getTipo() {
        return TipoActividad.EXAMEN;
    }
}
