package sv.gestionacademica.entity;

import java.time.LocalDate;

public class Tarea extends ActividadEvaluativa {

    public Tarea(int idActividad, String nombreActividad, String descripcion,
                 LocalDate fechaLimite, double ponderacion) {
        super(idActividad, nombreActividad, descripcion, fechaLimite, ponderacion);
    }

    // Tarea: si se entrega tarde, pierde 20% de la nota
    @Override
    public double calcularNotaFinal(double notaObtenida) {
        if (estaVencida()) {
            return notaObtenida * 0.8;
        }
        return notaObtenida;
    }
}