package com.example.SlushyApp.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "Pagos")
public class Pago {

    @Id
    private String id;
    private String reservaId;
    private double monto;
    private LocalDateTime fechaPago;
    private PaymentStatus status;  // ej. “SIMULADO”
    private PaymentMethod metodo;  // ej. “SIMULADO”

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReservaId() {
        return reservaId;
    }

    public void setReservaId(String reservaId) {
        this.reservaId = reservaId;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public PaymentMethod getMetodo() {
        return metodo;
    }

    public void setMetodo(PaymentMethod metodo) {
        this.metodo = metodo;
    }
}
