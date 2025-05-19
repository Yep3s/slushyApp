package com.example.SlushyApp.DTO;

import java.time.LocalDateTime;

public class HistorialReservaDto {

    private String reservaId;
    private String servicioNombre;
    private LocalDateTime fechaInicio;
    private String estado;
    private String placaVehiculo;
    private double monto;

    public HistorialReservaDto(String reservaId, String servicioNombre, LocalDateTime fechaInicio, String estado, String placaVehiculo, double monto) {
        this.reservaId = reservaId;
        this.servicioNombre = servicioNombre;
        this.fechaInicio = fechaInicio;
        this.estado = estado;
        this.placaVehiculo = placaVehiculo;
        this.monto = monto;
    }

    public HistorialReservaDto() {

    }

    public String getReservaId() {
        return reservaId;
    }

    public void setReservaId(String reservaId) {
        this.reservaId = reservaId;
    }

    public String getServicioNombre() {
        return servicioNombre;
    }

    public void setServicioNombre(String servicioNombre) {
        this.servicioNombre = servicioNombre;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPlacaVehiculo() {
        return placaVehiculo;
    }

    public void setPlacaVehiculo(String placaVehiculo) {
        this.placaVehiculo = placaVehiculo;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    @Override
    public String toString() {
        return "HistorialReservaDto{" +
                "reservaId='" + reservaId + '\'' +
                ", servicioNombre='" + servicioNombre + '\'' +
                ", fechaInicio=" + fechaInicio +
                ", estado='" + estado + '\'' +
                ", placaVehiculo='" + placaVehiculo + '\'' +
                ", monto=" + monto +
                '}';
    }
}
