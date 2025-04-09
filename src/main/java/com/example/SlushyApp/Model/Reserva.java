package com.example.SlushyApp.Model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "Reservas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    @Id
    private String id;

    @NotBlank(message = "La placa del vehículo es obligatoria")
    private String placaVehiculo;

    @NotBlank(message = "El nombre del servicio es obligatorio")
    private String servicioNombre;

    private LocalDateTime fechaReserva;

    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    private String observaciones;

    private String usuarioEmail;

    private int progreso; // Porcentaje de progreso del lavado (0 a 100)


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlacaVehiculo() {
        return placaVehiculo;
    }

    public void setPlacaVehiculo(String placaVehiculo) {
        this.placaVehiculo = placaVehiculo;
    }

    public String getServicioNombre() {
        return servicioNombre;
    }

    public void setServicioNombre(String servicioNombre) {
        this.servicioNombre = servicioNombre;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getUsuarioEmail() {
        return usuarioEmail;
    }

    public void setUsuarioEmail(String usuarioEmail) {
        this.usuarioEmail = usuarioEmail;
    }

    public int getProgreso() {
        return progreso;
    }

    public void setProgreso(int progreso) {
        this.progreso = progreso;
    }

}