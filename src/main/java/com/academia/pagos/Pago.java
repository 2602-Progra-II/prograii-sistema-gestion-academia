package com.academia.pagos;

import java.time.LocalDate;

public class Pago {
    private int idPago;
    private double monto;
    private LocalDate fechaPago;
    private String rutaComprobante;
    private EstadoPago estadoPago;
    private String idEstudiante;
    private String idAsesorValidador;

    public Pago(int idPago, double monto, LocalDate fechaPago, String rutaComprobante, String idEstudiante) {
        this.idPago = idPago;
        this.monto = monto;
        this.fechaPago = fechaPago;
        this.rutaComprobante = rutaComprobante;
        this.estadoPago = EstadoPago.PENDIENTE;
        this.idEstudiante = idEstudiante;
        this.idAsesorValidador = null;
    }

    public boolean estaPendiente() {
        return this.estadoPago == EstadoPago.PENDIENTE;
    }

    public boolean estaAprobado() {
        return this.estadoPago == EstadoPago.APROBADO;
    }

    public boolean estaRechazado() {
        return this.estadoPago == EstadoPago.RECHAZADO;
    }

    public int getIdPago() { return idPago; }
    public void setIdPago(int idPago) { this.idPago = idPago; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }

    public String getRutaComprobante() { return rutaComprobante; }
    public void setRutaComprobante(String rutaComprobante) { this.rutaComprobante = rutaComprobante; }

    public EstadoPago getEstadoPago() { return estadoPago; }
    public void setEstadoPago(EstadoPago estadoPago) { this.estadoPago = estadoPago; }

    public String getIdEstudiante() { return idEstudiante; }
    public void setIdEstudiante(String idEstudiante) { this.idEstudiante = idEstudiante; }

    public String getIdAsesorValidador() { return idAsesorValidador; }
    public void setIdAsesorValidador(String idAsesorValidador) { this.idAsesorValidador = idAsesorValidador; }
}