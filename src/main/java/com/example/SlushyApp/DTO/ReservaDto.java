package com.example.SlushyApp.DTO;

import com.example.SlushyApp.Model.EstadoReserva;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class ReservaDto {

    private String id;
    private String placa;              // antes era placaVehiculo
    private String tipoVehiculo;       // AUTOMOVIL, CAMIONETA, MOTO
    private String servicioNombre;     // nombre del servicio

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaInicio; // antes usábamos fechaReserva

    private String usuarioEmail;
    private EstadoReserva estado;
    private int progreso;
    private String observaciones;
    private int duracionMinutos;


    public ReservaDto() {
    }

    public ReservaDto(String id, String placa, String tipoVehiculo, String servicioNombre, LocalDateTime fechaInicio, String usuarioEmail, EstadoReserva estado, int progreso, String observaciones, int duracionMinutos) {
        this.id = id;
        this.placa = placa;
        this.tipoVehiculo = tipoVehiculo;
        this.servicioNombre = servicioNombre;
        this.fechaInicio = fechaInicio;
        this.usuarioEmail = usuarioEmail;
        this.estado = estado;
        this.progreso = progreso;
        this.observaciones = observaciones;
        this.duracionMinutos = duracionMinutos;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getTipoVehiculo() {
        return tipoVehiculo;
    }

    public void setTipoVehiculo(String tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
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

    public String getUsuarioEmail() {
        return usuarioEmail;
    }

    public void setUsuarioEmail(String usuarioEmail) {
        this.usuarioEmail = usuarioEmail;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public int getProgreso() {
        return progreso;
    }

    public void setProgreso(int progreso) {
        this.progreso = progreso;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }
}
