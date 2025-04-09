package com.example.SlushyApp.Controller;

import com.example.SlushyApp.Model.Servicio;
import com.example.SlushyApp.Service.ServicioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/servicios")
public class ServiciosPublicosController {

    private final ServicioService servicioService;

    public ServiciosPublicosController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<Servicio>> listarServiciosPublicos() {
        return ResponseEntity.ok(servicioService.listarServicios());
    }
}
