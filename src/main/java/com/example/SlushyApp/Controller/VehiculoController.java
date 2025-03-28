package com.example.SlushyApp.Controller;

import com.example.SlushyApp.Model.Vehiculo;
import com.example.SlushyApp.Service.VehiculoService;
import com.example.SlushyApp.Utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Vehiculo> registrarVehiculo(@Valid @RequestBody Vehiculo vehiculo, HttpServletRequest request) {
        String email = jwtUtil.getEmailFromToken(extractTokenFromRequest(request));
        vehiculo.setUsuarioEmail(email); // Asignamos el dueño desde el token
        return ResponseEntity.ok(vehiculoService.registrarVehiculo(vehiculo));
    }

    // 📋 Listar vehículos del usuario autenticado
    @GetMapping("/mis-vehiculos")
    public ResponseEntity<List<Vehiculo>> obtenerVehiculos(HttpServletRequest request) {
        String email = jwtUtil.getEmailFromToken(extractTokenFromRequest(request));
        return ResponseEntity.ok(vehiculoService.obtenerVehiculosPorUsuario(email));
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
        vehiculoService.eliminarVehiculo(id);
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
