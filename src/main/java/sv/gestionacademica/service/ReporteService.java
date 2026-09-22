package sv.gestionacademica.service;

import sv.gestionacademica.entity.IndicadorMora;

public class ReporteService {


    public IndicadorMora generarIndicadorMora(int totalEstudiantesEnMora, double montoTotalPendiente, String periodoAcademico) {
        return new IndicadorMora(totalEstudiantesEnMora, montoTotalPendiente, periodoAcademico);
    }


    public void calcularRendimientoCurso(String nombreCurso, double promedio, int aprobados, int reprobados) {
        System.out.println("Curso: " + nombreCurso);
        System.out.println("Promedio general: " + promedio);
        System.out.println("Estudiantes aprobados: " + aprobados);
        System.out.println("Estudiantes reprobados: " + reprobados);
    }
}
