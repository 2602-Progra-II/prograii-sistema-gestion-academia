package sv.gestionacademica.entity;

public class IndicadorMora {
    private int totalEstudiantesEnMora;
    private double montoTotalPendiente;
    private String periodoAcademico;

    public IndicadorMora(int totalEstudiantesEnMora, double montoTotalPendiente, String periodoAcademico) {
        this.totalEstudiantesEnMora = totalEstudiantesEnMora;
        this.montoTotalPendiente = montoTotalPendiente;
        this.periodoAcademico = periodoAcademico;
    }


    public int getTotalEstudiantesEnMora() {
        return totalEstudiantesEnMora;
    }

    public void setTotalEstudiantesEnMora(int totalEstudiantesEnMora) {
        this.totalEstudiantesEnMora = totalEstudiantesEnMora;
    }

    public double getMontoTotalPendiente() {
        return montoTotalPendiente;
    }

    public void setMontoTotalPendiente(double montoTotalPendiente) {
        this.montoTotalPendiente = montoTotalPendiente;
    }

    public String getPeriodoAcademico() {
        return periodoAcademico;
    }

    public void setPeriodoAcademico(String periodoAcademico) {
        this.periodoAcademico = periodoAcademico;
    }
}