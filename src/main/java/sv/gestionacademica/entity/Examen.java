package sv.gestionacademica.entity;

import java.time.LocalDate;

public class Examen extends ActividadEvaluativa {

    private static final double NOTA_MINIMA = 6.0;

    public Examen(int idActividad, String nombreActividad, String descripcion,
                  LocalDate fechaLimite, double ponderacion) {
        super(idActividad, nombreActividad, descripcion, fechaLimite, ponderacion);
    }

    // Examen: no se castiga por fecha, pero si la nota es menor al mínimo, se reporta tal cual
    @Override
    public double calcularNotaFinal(double notaObtenida) {
        return Math.max(notaObtenida, 0.0);
    }

    public boolean estaAprobado(double notaObtenida) {
        return notaObtenida >= NOTA_MINIMA;
    }
}