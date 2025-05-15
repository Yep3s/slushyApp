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

    /**
     * Crea un nuevo servicio, asegurándose de que no exista otro con el mismo nombre.
     */
    public Servicio crearServicio(Servicio servicio) {
        if (servicioRepository.existsByNombre(servicio.getNombre())) {
            throw new RuntimeException("Ya existe un servicio con ese nombre.");
        }
        return servicioRepository.save(servicio);
    }

    /**
     * Actualiza un servicio existente copiando todos los campos del objeto recibido.
     */
    public Servicio actualizarServicio(String id, Servicio servicioActualizado) {
        Servicio existente = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        existente.setNombre(servicioActualizado.getNombre());
        existente.setDescripcion(servicioActualizado.getDescripcion());
        // Ahora trabajamos con el mapa de precios por tipo de vehículo:
        existente.setPreciosPorTipo(servicioActualizado.getPreciosPorTipo());
        existente.setDuracionMinutos(servicioActualizado.getDuracionMinutos());
        existente.setEstado(servicioActualizado.getEstado());

        return servicioRepository.save(existente);
    }

    /**
     * Elimina un servicio por su id.
     */
    public void eliminarServicio(String id) {

         servicioRepository.deleteById(id);
    }

    /**
     * Lista todos los servicios.
     */
    public List<Servicio> listarServicios() {
        return servicioRepository.findAll();
    }

    /**
     * Obtiene un servicio por su id.
     */
    public Optional<Servicio> obtenerPorId(String id) {
        return servicioRepository.findById(id);
    }
}
