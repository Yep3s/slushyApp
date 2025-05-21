package com.example.SlushyApp.DTO;

public class DashboardStatsDto {

    private long vehiculosHoy;
    private double ingresosHoy;
    private long clientesNuevos;

    public DashboardStatsDto() {
    }

    public DashboardStatsDto(long vehiculosHoy, double ingresosHoy, long clientesNuevos) {
        this.vehiculosHoy = vehiculosHoy;
        this.ingresosHoy = ingresosHoy;
        this.clientesNuevos = clientesNuevos;
    }

    public long getVehiculosHoy() {
        return vehiculosHoy;
    }

    public void setVehiculosHoy(long vehiculosHoy) {
        this.vehiculosHoy = vehiculosHoy;
    }

    public double getIngresosHoy() {
        return ingresosHoy;
    }

    public void setIngresosHoy(double ingresosHoy) {
        this.ingresosHoy = ingresosHoy;
    }

    public long getClientesNuevos() {
        return clientesNuevos;
    }

    public void setClientesNuevos(long clientesNuevos) {
        this.clientesNuevos = clientesNuevos;
    }

    @Override
    public String toString() {
        return "DashboardStatsDto{" +
                "vehiculosHoy=" + vehiculosHoy +
                ", ingresosHoy=" + ingresosHoy +
                ", clientesNuevos=" + clientesNuevos +
                '}';
    }
}
