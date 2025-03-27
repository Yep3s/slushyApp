package com.example.SlushyApp.Controller;

import com.example.SlushyApp.Model.Servicio;
import com.example.SlushyApp.Service.ServicioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @PostMapping("/crearServicio")
    public ResponseEntity<Servicio> crearServicio(@Valid @RequestBody Servicio servicio) {
        return ResponseEntity.ok(servicioService.crearServicio(servicio));
    }

    @PutMapping("/actualizarServicio/{id}")
    public ResponseEntity<Servicio> actualizarServicio(@PathVariable String id, @Valid @RequestBody Servicio servicio) {
        return ResponseEntity.ok(servicioService.actualizarServicio(id, servicio));
    }

    @DeleteMapping("/eliminarServicio/{id}")
    public ResponseEntity<String> eliminarServicio(@PathVariable String id) {
        servicioService.eliminarServicio(id);
        return ResponseEntity.ok("Servicio eliminado correctamente.");
    }

    @GetMapping("/listarServicios")
    public ResponseEntity<List<Servicio>> listarServicios() {
        return ResponseEntity.ok(servicioService.listarServicios());
    }

    @GetMapping("/listarServicio/{id}")
    public ResponseEntity<Servicio> obtenerPorId(@PathVariable String id) {
        return servicioService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
