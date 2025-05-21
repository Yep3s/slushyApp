package com.example.SlushyApp.Controller;

import com.example.SlushyApp.DTO.ReservaDto;
import com.example.SlushyApp.Model.EstadoReserva;
import com.example.SlushyApp.Model.Reserva;
import com.example.SlushyApp.Model.Servicio;
import com.example.SlushyApp.Model.Vehiculo;
import com.example.SlushyApp.Service.ReservaService;
import com.example.SlushyApp.Service.ServicioService;
import com.example.SlushyApp.Service.VehiculoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/empleado/reservas")
@PreAuthorize("hasAuthority('EMPLOYEE')")
public class ReservaEmpleadoController {

    private final ReservaService   reservaService;
    private final ServicioService  servicioService;
    private final VehiculoService  vehiculoService;

    public ReservaEmpleadoController(ReservaService reservaService,
                                     ServicioService servicioService,
                                     VehiculoService vehiculoService) {
        this.reservaService  = reservaService;
        this.servicioService = servicioService;
        this.vehiculoService = vehiculoService;
    }

    // Helper para mapear Reserva → ReservaDto
    private ReservaDto toDto(Reserva r) {
        // Obtener Servicio (Optional<Servicio>)
        Optional<Servicio> srvOpt = servicioService.obtenerPorId(r.getServicioId());
        Servicio srv = srvOpt.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Servicio no encontrado: " + r.getServicioId())
        );

        // Obtener Vehículo (List<Vehiculo>, puede venir vacío)
        List<Vehiculo> vehList = vehiculoService.buscarPorPlaca(r.getPlacaVehiculo());
        Vehiculo veh = vehList.stream().findFirst().orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Vehículo no encontrado: " + r.getPlacaVehiculo())
        );

        return new ReservaDto(
                r.getId(),
                r.getPlacaVehiculo(),
                veh.getTipoVehiculo().name(),
                srv.getNombre(),
                r.getFechaInicio(),
                r.getUsuarioEmail(),
                r.getEstado(),
                r.getProgreso(),
                r.getObservaciones(),
                srv.getDuracionMinutos()      // ¡aquí!
        );
    }

    /** Listar reservas en PENDIENTE */
    @GetMapping("/pendientes")
    public ResponseEntity<List<ReservaDto>> verPendientes() {
        List<ReservaDto> dtos = reservaService
                .obtenerReservasPorEstado(EstadoReserva.PENDIENTE)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /** Confirmar llegada: PENDIENTE → CONFIRMADA */
    @PutMapping("/confirmar/{id}")
    public ResponseEntity<ReservaDto> confirmar(@PathVariable String id) {
        Reserva updated = reservaService.actualizarEstadoComoEmpleado(id, EstadoReserva.CONFIRMADA);
        return ResponseEntity.ok(toDto(updated));
    }

    /** Iniciar servicio: CONFIRMADA → EN_PROCESO */
    @PutMapping("/iniciar/{id}")
    public ResponseEntity<ReservaDto> iniciarServicio(@PathVariable String id) {
        Reserva updated = reservaService.actualizarEstadoComoEmpleado(id, EstadoReserva.EN_PROCESO);
        return ResponseEntity.ok(toDto(updated));
    }

    /** Completar servicio: EN_PROCESO → COMPLETADA */
    @PutMapping("/completar/{id}")
    public ResponseEntity<ReservaDto> completarServicio(@PathVariable String id) {
        Reserva updated = reservaService.actualizarEstadoComoEmpleado(id, EstadoReserva.COMPLETADA);
        return ResponseEntity.ok(toDto(updated));
    }

    /** Listar reservas en CONFIRMADA */
    @GetMapping("/confirmadas")
    public ResponseEntity<List<ReservaDto>> verConfirmadas() {
        List<ReservaDto> dtos = reservaService
                .obtenerReservasPorEstado(EstadoReserva.CONFIRMADA)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /** Listar reservas en EN_PROCESO */
    @GetMapping("/enProceso")
    public ResponseEntity<List<ReservaDto>> verEnProceso() {
        List<ReservaDto> dtos = reservaService
                .obtenerReservasPorEstado(EstadoReserva.EN_PROCESO)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /** Cambiar cualquier estado vía query param */
    @PutMapping("/estado/{id}")
    public ResponseEntity<ReservaDto> cambiarEstado(
            @PathVariable String id,
            @RequestParam("nuevoEstado") EstadoReserva nuevoEstado
    ) {
        Reserva updated = reservaService.actualizarEstadoComoEmpleado(id, nuevoEstado);
        return ResponseEntity.ok(toDto(updated));
    }

    /** Actualizar progreso (0–100%) */
    @PutMapping("/progreso/{id}")
    public ResponseEntity<ReservaDto> actualizarProgreso(
            @PathVariable String id,
            @RequestParam("progreso") int progreso
    ) {
        Reserva updated = reservaService.actualizarProgresoReserva(id, progreso);
        return ResponseEntity.ok(toDto(updated));
    }

    /** Listar todas las reservas sin filtrar */
    @GetMapping
    public ResponseEntity<List<ReservaDto>> verTodas() {
        List<ReservaDto> dtos = reservaService
                .obtenerTodasLasReservas()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
