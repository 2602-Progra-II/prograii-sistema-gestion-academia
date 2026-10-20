package sv.gestionacademica.entity;

import java.time.LocalDate;

public class Proyecto extends ActividadEvaluativa {

    public Proyecto(int idActividad, String nombreActividad, String descripcion,
                    LocalDate fechaLimite, double ponderacion) {
        super(idActividad, nombreActividad, descripcion, fechaLimite, ponderacion);
    }

    // Proyecto: bonificación de 5% si se entrega antes de la fecha límite
    @Override
    public double calcularNotaFinal(double notaObtenida) {
        if (!estaVencida()) {
            return Math.min(notaObtenida * 1.05, 10.0);
        }
        return notaObtenida;
    }
}