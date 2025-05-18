package com.example.SlushyApp.Controller;

import com.example.SlushyApp.Model.EstadoReserva;
import com.example.SlushyApp.Model.Reserva;
import com.example.SlushyApp.Service.ReservaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/empleado/reservas")
// Solo usuarios con rol EMPLEADO podrán invocar estos endpoints
@PreAuthorize("hasAuthority('EMPLEADO')")
public class ReservaEmpleadoController {

    private final ReservaService reservaService;

    public ReservaEmpleadoController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    /**
     * 🔍 Listar todas las reservas que están en estado PENDIENTE
     */
    @GetMapping("/pendientes")
    public ResponseEntity<List<Reserva>> verPendientes() {
        List<Reserva> pendientes = reservaService.obtenerReservasPorEstado(EstadoReserva.PENDIENTE);
        return ResponseEntity.ok(pendientes);
    }

    /**
     * ✅ Confirmar (marcar como CONFIRMADA) cuando el cliente llega.
     */
    @PutMapping("/confirmar/{id}")
    public ResponseEntity<Reserva> confirmar(@PathVariable String id) {
        Reserva actualizada = reservaService.actualizarEstadoComoEmpleado(id, EstadoReserva.CONFIRMADA);
        return ResponseEntity.ok(actualizada);
    }

    /**
     * 🔄 Cambiar el estado arbitrariamente (pasar a CANCELADA, REPROGRAMADA, etc.)
     */
    @PutMapping("/estado/{id}")
    public ResponseEntity<Reserva> cambiarEstado(
            @PathVariable String id,
            @RequestParam("nuevoEstado") EstadoReserva nuevoEstado
    ) {
        Reserva actualizada = reservaService.actualizarEstadoComoEmpleado(id, nuevoEstado);
        return ResponseEntity.ok(actualizada);
    }

    /**
     * 🟢 Obtener todas las reservas (independiente de su estado)
     */
    @GetMapping
    public ResponseEntity<List<Reserva>> verTodas() {
        List<Reserva> todas = reservaService.obtenerTodasLasReservas();
        return ResponseEntity.ok(todas);
    }

    /**
     * 🚗 Actualizar el progreso (0–100%) de una reserva en curso, para tu barra de progreso estilo Domino’s.
     */
    @PutMapping("/progreso/{id}")
    public ResponseEntity<Reserva> actualizarProgreso(
            @PathVariable String id,
            @RequestParam("progreso") int progreso
    ) {
        Reserva actualizada = reservaService.actualizarProgresoReserva(id, progreso);
        return ResponseEntity.ok(actualizada);
    }
}
