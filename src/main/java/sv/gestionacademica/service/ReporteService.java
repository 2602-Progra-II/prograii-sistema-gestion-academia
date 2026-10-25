package sv.gestionacademica.service;

import sv.gestionacademica.entity.EstadoCuenta;
import sv.gestionacademica.entity.IndicadorMora;

import java.util.List;

public class ReporteService {


    public IndicadorMora generarIndicadorMora(int totalEstudiantesEnMora, double montoTotalPendiente, String periodoAcademico) {
        return new IndicadorMora(totalEstudiantesEnMora, montoTotalPendiente, periodoAcademico);
    }

    /**
     * Indicador de mora a partir de estados de cuenta reales
     * (los que devuelve EstadoCuentaService.calcularTodosDesdeBD()).
     * montoTotalPendiente suma el saldo de los estudiantes en mora.
     */
    public IndicadorMora generarIndicadorMora(List<EstadoCuenta> cuentas, String periodoAcademico) {
        int enMora = 0;
        double montoPendiente = 0;
        for (EstadoCuenta c : cuentas) {
            if (c.isEnMora()) {
                enMora++;
                montoPendiente += c.getSaldoPendiente();
            }
        }
        return generarIndicadorMora(enMora, montoPendiente, periodoAcademico);
    }


    public void calcularRendimientoCurso(String nombreCurso, double promedio, int aprobados, int reprobados) {
        System.out.println("Curso: " + nombreCurso);
        System.out.println("Promedio general: " + promedio);
        System.out.println("Estudiantes aprobados: " + aprobados);
        System.out.println("Estudiantes reprobados: " + reprobados);
    }
}
