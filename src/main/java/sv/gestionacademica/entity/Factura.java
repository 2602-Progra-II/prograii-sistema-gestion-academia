package sv.gestionacademica.entity;

import java.time.LocalDate;

public class Factura {

    private int idFactura;
    private String numeroFactura;
    private LocalDate fechaEmision;
    private double montoTotal;

    public Factura(int idFactura, String numeroFactura, LocalDate fechaEmision, double montoTotal) {
        this.idFactura = idFactura;
        this.numeroFactura = numeroFactura;
        this.fechaEmision = fechaEmision;
        this.montoTotal = montoTotal;
    }

    public int getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(int idFactura) {
        this.idFactura = idFactura;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(double montoTotal) {
        this.montoTotal = montoTotal;
    }
}