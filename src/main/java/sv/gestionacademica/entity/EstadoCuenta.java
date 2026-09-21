package sv.gestionacademica.entity;

import java.util.Date;

public class EstadoCuenta {
    private String idEstudiante;
    private String nombreEstudiante;
    private double saldoPendiente;
    private boolean enMora;
    private Date fechaUltimaActualizacion;

    public EstadoCuenta(String idEstudiante, String nombreEstudiante, double saldoPendiente, boolean enMora) {
        this.idEstudiante = idEstudiante;
        this.nombreEstudiante = nombreEstudiante;
        this.saldoPendiente = saldoPendiente;
        this.enMora = enMora;
        this.fechaUltimaActualizacion = new Date();
    }


    public String getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(String idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public String getNombreEstudiante() {
        return nombreEstudiante;
    }

    public void setNombreEstudiante(String nombreEstudiante) {
        this.nombreEstudiante = nombreEstudiante;
    }

    public double getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(double saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }

    public boolean isEnMora() {
        return enMora;
    }

    public void setEnMora(boolean enMora) {
        this.enMora = enMora;
    }

    public Date getFechaUltimaActualizacion() {
        return fechaUltimaActualizacion;
    }

    public void setFechaUltimaActualizacion(Date fechaUltimaActualizacion) {
        this.fechaUltimaActualizacion = fechaUltimaActualizacion;
    }
}