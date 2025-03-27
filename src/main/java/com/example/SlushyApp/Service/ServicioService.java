package com.example.SlushyApp.Service;

import com.example.SlushyApp.Model.Servicio;
import com.example.SlushyApp.Repository.ServicioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioService(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    public Servicio crearServicio(Servicio servicio) {
        if (servicioRepository.existsByNombre(servicio.getNombre())) {
            throw new RuntimeException("Ya existe un servicio con ese nombre.");
        }
        return servicioRepository.save(servicio);
    }

    public Servicio actualizarServicio(String id, Servicio servicioActualizado) {
        Servicio existente = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        existente.setNombre(servicioActualizado.getNombre());
        existente.setDescripcion(servicioActualizado.getDescripcion());
        existente.setPrecio(servicioActualizado.getPrecio());
        existente.setDuracionMinutos(servicioActualizado.getDuracionMinutos());

        return servicioRepository.save(existente);
    }

    public void eliminarServicio(String id) {
        servicioRepository.deleteById(id);
    }

    public List<Servicio> listarServicios() {
        return servicioRepository.findAll();
    }

    public Optional<Servicio> obtenerPorId(String id) {
        return servicioRepository.findById(id);
    }



}
