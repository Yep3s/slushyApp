package com.example.SlushyApp.Service;

import com.example.SlushyApp.Exceptions.PlacaYaRegistradaException;
import com.example.SlushyApp.Model.Vehiculo;
import com.example.SlushyApp.Repository.VehiculoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }

    // Registrar vehículo
    public Vehiculo registrarVehiculo(Vehiculo vehiculo) {
        if (vehiculoRepository.existsByPlaca(vehiculo.getPlaca())) {
            throw new PlacaYaRegistradaException("Error: La placa ya está registrada.");
        }
        return vehiculoRepository.save(vehiculo);
    }

    // Obtener vehículos por email de usuario
    public List<Vehiculo> obtenerVehiculosPorUsuario(String usuarioEmail) {
        return vehiculoRepository.findByUsuarioEmail(usuarioEmail);
    }

    // Eliminar vehículo por ID
    public void eliminarVehiculo(String id) {
        vehiculoRepository.deleteById(id);
    }

    public Vehiculo actualizarVehiculo(String id, Vehiculo vehiculoActualizado) {
        Vehiculo existente = vehiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehículo no encontrado"));

        // Actualiza los campos permitidos
        existente.setMarca(vehiculoActualizado.getMarca());
        existente.setLinea(vehiculoActualizado.getLinea());
        existente.setModelo(vehiculoActualizado.getModelo());
        existente.setColor(vehiculoActualizado.getColor());
        existente.setTipoVehiculo(vehiculoActualizado.getTipoVehiculo());

        return vehiculoRepository.save(existente);
    }



}
