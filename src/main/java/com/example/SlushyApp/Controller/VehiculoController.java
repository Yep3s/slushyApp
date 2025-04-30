package com.example.SlushyApp.Controller;

import com.example.SlushyApp.Model.Vehiculo;
import com.example.SlushyApp.Service.VehiculoService;
import com.example.SlushyApp.Utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/vehiculos")
public class VehiculoController {

    private final VehiculoService vehiculoService;
    private final JwtUtil jwtUtil;

    public VehiculoController(VehiculoService vehiculoService, JwtUtil jwtUtil) {
        this.vehiculoService = vehiculoService;
        this.jwtUtil = jwtUtil;
    }

    // 🚘 Registrar vehículo (asignando email desde token JWT)
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarVehiculo(@Valid @RequestBody Vehiculo vehiculo, HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromCookie(request);

        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT no encontrado o inválido");
        }

        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT inválido");
        }

        String email = jwtUtil.getEmailFromToken(token);
        vehiculo.setUsuarioEmail(email);
        return ResponseEntity.ok(vehiculoService.registrarVehiculo(vehiculo));
    }


    // 📋 Listar vehículos del usuario autenticado
    @GetMapping("/mis-vehiculos")
    public String mostrarVehiculosUsuario(HttpServletRequest request, Model model) {
        String token = jwtUtil.extractTokenFromCookie(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return "redirect:/login";
        }

        String email = jwtUtil.getEmailFromToken(token);
        List<Vehiculo> vehiculos = vehiculoService.obtenerVehiculosPorUsuario(email);
        model.addAttribute("vehiculos", vehiculos);

        return "userDashboard"; // o el nombre de tu plantilla
    }


    // ✏️ Actualizar un vehículo por ID
    @PutMapping("/editar/{id}")
    public ResponseEntity<Vehiculo> actualizarVehiculo(@PathVariable String id, @Valid @RequestBody Vehiculo vehiculoActualizado, HttpServletRequest request) {
        String email = jwtUtil.getEmailFromToken(extractTokenFromRequest(request));
        vehiculoActualizado.setUsuarioEmail(email); // Mantén el email del dueño actual
        return ResponseEntity.ok(vehiculoService.actualizarVehiculo(id, vehiculoActualizado));
    }


    // ❌ Eliminar vehículo
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminarVehiculo(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuarioEmail = auth.getName(); // Esto asume que el nombre de usuario es el email
        System.out.println("ID recibido para eliminar: " + id);
        vehiculoService.eliminarVehiculo(id, usuarioEmail);
        return ResponseEntity.ok("Vehículo eliminado correctamente.");
    }

    // 🔐 Extraer token JWT del header Authorization
    private String extractTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.replace("Bearer ", "");
        }
        return null;
    }



}
