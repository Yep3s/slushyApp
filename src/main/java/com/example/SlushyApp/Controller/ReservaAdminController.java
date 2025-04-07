package com.example.SlushyApp.Controller;

import com.example.SlushyApp.Model.EstadoReserva;
import com.example.SlushyApp.Model.Reserva;
import com.example.SlushyApp.Service.ReservaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/reservas")
public class ReservaAdminController {

    private final ReservaService reservaService;

    public ReservaAdminController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    // 🔍 1 y 2: Ver todas las reservas o filtrarlas
    @GetMapping
    public ResponseEntity<List<Reserva>> filtrarReservas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) EstadoReserva estado,
            @RequestParam(required = false) String emailCliente
    ) {
        LocalDateTime fechaInicio = (fecha != null) ? fecha.atStartOfDay() : null;
        return ResponseEntity.ok(reservaService.filtrarReservas(fechaInicio, estado, emailCliente));
    }






}
