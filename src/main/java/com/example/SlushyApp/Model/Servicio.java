package com.example.SlushyApp.Model;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "Servicios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Servicio {

    @Id
    private String id;

    @NotBlank(message = "El nombre no puede estar vacío.")
    private String nombre;

    @NotBlank(message = "La descripción no puede estar vacía.")
    private String descripcion;


    private Map<TipoVehiculo, Double> preciosPorTipo;

    @Min(value = 1, message = "La duración debe ser mayor a 0 minutos.")
    private int duracionMinutos;

    private EstadoServicio estado;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Map<TipoVehiculo, Double> getPreciosPorTipo() {
        return preciosPorTipo;
    }

    public void setPreciosPorTipo(Map<TipoVehiculo, Double> preciosPorTipo) {
        this.preciosPorTipo = preciosPorTipo;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public EstadoServicio getEstado() {
        return estado;
    }

    public void setEstado(EstadoServicio estado) {
        this.estado = estado;
    }
}


