package com.example.SlushyApp.Service;

import com.example.SlushyApp.Exceptions.PlacaYaRegistradaException;
import com.example.SlushyApp.Model.Vehiculo;
import com.example.SlushyApp.Repository.VehiculoRepository;
import org.springframework.security.access.AccessDeniedException;
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
    public void eliminarVehiculo(String id, String emailUsuario) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehículo no encontrado"));

        if (!vehiculo.getUsuarioEmail().equals(emailUsuario)) {
            throw new AccessDeniedException("No puedes eliminar este vehículo.");
        }

        vehiculoRepository.deleteById(id);
    }

    //Actualizar Vehiculo
    public Vehiculo actualizarVehiculo(String id, Vehiculo vehiculoActualizado) {
        Vehiculo existente = vehiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehículo no encontrado"));

        // Actualiza los campos permitidos
        existente.setPlaca(vehiculoActualizado.getPlaca());
        existente.setMarca(vehiculoActualizado.getMarca());
        existente.setLinea(vehiculoActualizado.getLinea());
        existente.setModelo(vehiculoActualizado.getModelo());
        existente.setColor(vehiculoActualizado.getColor());
        existente.setTipoVehiculo(vehiculoActualizado.getTipoVehiculo());
        existente.setUsuarioEmail(vehiculoActualizado.getUsuarioEmail());



        return vehiculoRepository.save(existente);
    }

    //nuevas cosas

    // Eliminar vehículo como admin (sin validar dueño)
    public void eliminarVehiculoComoAdmin(String id) {
        if (!vehiculoRepository.existsById(id)) {
            throw new RuntimeException("Vehículo no encontrado");
        }
        vehiculoRepository.deleteById(id);
    }

    // ✅ Obtener todos los vehículos
    public List<Vehiculo> obtenerTodos() {
        return vehiculoRepository.findAll();
    }

    // ✅ Contar total
    public long contarTodos() {
        return vehiculoRepository.count();
    }

    // ✅ Buscar por placa parcial
    public List<Vehiculo> buscarPorPlaca(String placa) {
        return vehiculoRepository.findByPlacaContainingIgnoreCase(placa);
    }

    // ✅ Filtrar por tipo
    public List<Vehiculo> filtrarPorTipo(String tipo) {
        return vehiculoRepository.findByTipoVehiculo(tipo);
    }







}
