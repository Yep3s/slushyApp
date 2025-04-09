package com.example.SlushyApp.Controller;

import com.example.SlushyApp.Model.EstadoReserva;
import com.example.SlushyApp.Model.Reserva;
import com.example.SlushyApp.Service.ReservaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/empleado/reservas")
public class ReservaEmpleadoController {

    private final ReservaService reservaService;

    public ReservaEmpleadoController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    // 🔍 Ver todas las reservas pendientes
    @GetMapping("/pendientes")
    public ResponseEntity<List<Reserva>> verPendientes() {
        return ResponseEntity.ok(reservaService.obtenerReservasPorEstado(EstadoReserva.PENDIENTE));
    }

    // ✅ Confirmar una reserva (cuando el cliente llega)
    @PutMapping("/confirmar/{id}")
    public ResponseEntity<Reserva> confirmar(@PathVariable String id) {
        return ResponseEntity.ok(reservaService.actualizarEstadoComoEmpleado(id, EstadoReserva.CONFIRMADA));
    }

    // 🔄 Cambiar estado de una reserva como empleado
    @PutMapping("/estado/{id}")
    public ResponseEntity<Reserva> cambiarEstado(
            @PathVariable String id,
            @RequestParam EstadoReserva nuevoEstado
    ) {
        return ResponseEntity.ok(reservaService.actualizarEstadoComoEmpleado(id, nuevoEstado));
    }

    // 🟢 Ver todas las reservas (sin filtro)
    @GetMapping
    public ResponseEntity<List<Reserva>> verTodas() {
        return ResponseEntity.ok(reservaService.obtenerTodasLasReservas());
    }

    // 🚗 Actualizar progreso de una reserva (para barra estilo Domino's)
    @PutMapping("/progreso/{id}")
    public ResponseEntity<Reserva> actualizarProgreso(
            @PathVariable String id,
            @RequestParam int progreso
    ) {
        return ResponseEntity.ok(reservaService.actualizarProgresoReserva(id, progreso));
    }
}
