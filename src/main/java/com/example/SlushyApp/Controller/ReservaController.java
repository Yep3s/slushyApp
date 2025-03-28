package com.example.SlushyApp.Controller;

import com.example.SlushyApp.Model.EstadoReserva;
import com.example.SlushyApp.Model.Reserva;
import com.example.SlushyApp.Service.ReservaService;
import com.example.SlushyApp.Utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/user/reservas")
public class ReservaController {

    private final ReservaService reservaService;
    private final JwtUtil jwtUtil;

    public ReservaController(ReservaService reservaService, JwtUtil jwtUtil) {
        this.reservaService = reservaService;
        this.jwtUtil = jwtUtil;
    }

    // 🟢 Crear reserva
    @PostMapping("/crear")
    public ResponseEntity<Reserva> crearReserva(@RequestBody @Valid Reserva reserva, HttpServletRequest request) {
        String email = getEmailFromToken(request);
        return ResponseEntity.ok(reservaService.crearReserva(reserva, email));
    }

    // 📋 Ver reservas del usuario autenticado
    @GetMapping("/mis-reservas")
    public ResponseEntity<List<Reserva>> obtenerReservas(HttpServletRequest request) {
        String email = getEmailFromToken(request);
        return ResponseEntity.ok(reservaService.obtenerReservasPorUsuario(email));
    }

    // ❌ Cancelar una reserva
    @PutMapping("/cancelar/{id}")
    public ResponseEntity<Reserva> cancelarReserva(@PathVariable String id, HttpServletRequest request) {
        String email = getEmailFromToken(request);
        return ResponseEntity.ok(reservaService.cambiarEstadoReserva(id, EstadoReserva.CANCELADA, email));
    }

    // ✅ Confirmar una reserva
    @PutMapping("/confirmar/{id}")
    public ResponseEntity<Reserva> confirmarReserva(@PathVariable String id, HttpServletRequest request) {
        String email = getEmailFromToken(request);
        return ResponseEntity.ok(reservaService.cambiarEstadoReserva(id, EstadoReserva.CONFIRMADA, email));
    }

    // 🔄 Reprogramar una reserva
    @PutMapping("/reprogramar/{id}")
    public ResponseEntity<Reserva> reprogramarReserva(
            @PathVariable String id,
            @RequestParam("nuevaFecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime nuevaFecha,
            HttpServletRequest request
    ) {
        String email = getEmailFromToken(request);
        return ResponseEntity.ok(reservaService.reprogramarReserva(id, nuevaFecha, email));
    }

    // 🔐 Extraer email desde el token
    private String getEmailFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return jwtUtil.getEmailFromToken(token);
    }


}
