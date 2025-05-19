package com.example.SlushyApp.Controller;

import com.example.SlushyApp.Model.*;
import com.example.SlushyApp.Repository.PagoRepository;
import com.example.SlushyApp.Repository.ServicioRepository;
import com.example.SlushyApp.Repository.VehiculoRepository;
import com.example.SlushyApp.Service.ReservaService;
import com.example.SlushyApp.Utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user/reservas")
public class ReservaController {

    private final PagoRepository pagoRepository;
    private final ServicioRepository servicioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ReservaService reservaService;
    private final JwtUtil jwtUtil;

    public ReservaController(PagoRepository pagoRepository, ServicioRepository servicioRepository ,VehiculoRepository vehiculoRepository,ReservaService reservaService, JwtUtil jwtUtil) {
        this.reservaService = reservaService;
        this.jwtUtil = jwtUtil;
        this.vehiculoRepository = vehiculoRepository;
        this.servicioRepository = servicioRepository;
        this.pagoRepository = pagoRepository;
    }

    // 🔍 Obtener franjas libres para un servicio en un día
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalDateTime>> disponibilidad(
            @RequestParam("servicioId") String servicioId,
            @RequestParam("fecha")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpServletRequest request
    ) {
        String email = getEmailFromToken(request);
        List<LocalDateTime> slots = reservaService.obtenerSlotsDisponibles(servicioId, fecha);
        return ResponseEntity.ok(slots);
    }

    // 🟢 Crear reserva
    @PostMapping("/crear")
    public ResponseEntity<Map<String,Object>> crearReserva(
            @RequestBody @Valid Reserva nueva,
            HttpServletRequest request
    ) {
        String email = getEmailFromToken(request);

        // 1) guardamos la reserva
        Reserva creada = reservaService.crearReserva(nueva, email);

        // 2) recuperamos el pago simulado
        Pago pago = reservaService.simularPago(creada);

        // 3) devolvemos ambos
        Map<String,Object> resp = new HashMap<>();
        resp.put("reserva", creada);
        resp.put("pago",    pago);
        return ResponseEntity.ok(resp);
    }

    private String getEmailFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return jwtUtil.getEmailFromToken(token);
    }

    private String extractEmail(HttpServletRequest req) {
        // 1) Revisa header Authorization
        String auth = req.getHeader("Authorization");
        String token;
        if (auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7);
        } else {
            // 2) Si no viene en header, lo saca de la cookie “jwt”
            token = jwtUtil.extractTokenFromCookie(req);
        }
        return jwtUtil.getEmailFromToken(token);
    }

    /**
     * Devuelve sólo aquellos servicios cuyo map preciosPorTipo
     * contiene la clave igual al tipoVehiculo de la placa.
     */
    @GetMapping("/servicios-disponibles")  // ← nuevo endpoint con esto te muestra los servicios disponibles segun la placa y tipo de vehiculo
    public ResponseEntity<List<Servicio>> serviciosDisponibles(
            @RequestParam String placa,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) String email,           // ← opcional
            HttpServletRequest request
    ) {
        // si te pasaron email por parámetro, úsalo; sino extrae del JWT
        String usuarioEmail = (email != null && !email.isBlank())
                ? email.trim()
                : extractEmail(request);

        String placaLimpia = placa.trim().toUpperCase();
        Vehiculo veh = vehiculoRepository
                .findByPlacaAndUsuarioEmail(placaLimpia, usuarioEmail);
        if (veh == null) {
            throw new RuntimeException(
                    "No tienes asociado ningún vehículo con placa: " + placaLimpia
            );
        }

        List<Servicio> disponibles = servicioRepository.findAll().stream()
                .filter(s -> s.getPreciosPorTipo().containsKey(veh.getTipoVehiculo()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(disponibles);
    }




}
