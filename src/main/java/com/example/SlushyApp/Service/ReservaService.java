package com.example.SlushyApp.Service;

import com.example.SlushyApp.Model.EstadoReserva;
import com.example.SlushyApp.Model.Reserva;
import com.example.SlushyApp.Model.Vehiculo;
import com.example.SlushyApp.Repository.ReservaRepository;
import com.example.SlushyApp.Repository.VehiculoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final VehiculoRepository vehiculoRepository;

    public ReservaService(ReservaRepository reservaRepository, VehiculoRepository vehiculoRepository) {
        this.reservaRepository = reservaRepository;
        this.vehiculoRepository = vehiculoRepository;
    }

    // 1. Crear reserva
    public Reserva crearReserva(Reserva reserva, String usuarioEmail) {
        Vehiculo vehiculo = vehiculoRepository.findByPlaca(reserva.getPlacaVehiculo());
        if (vehiculo == null || !vehiculo.getUsuarioEmail().equals(usuarioEmail)) {
            throw new RuntimeException("No puedes reservar con un vehículo que no te pertenece.");
        }

        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setFechaReserva(reserva.getFechaReserva() != null ? reserva.getFechaReserva() : LocalDateTime.now());
        reserva.setUsuarioEmail(usuarioEmail); // ✅ Guardar email del usuario que hace la reserva

        return reservaRepository.save(reserva);
    }


    // 2. Obtener reservas por email del usuario
    public List<Reserva> obtenerReservasPorUsuario(String email) {
        List<Vehiculo> vehiculos = vehiculoRepository.findByUsuarioEmail(email);
        List<String> placas = vehiculos.stream().map(Vehiculo::getPlaca).collect(Collectors.toList());
        return reservaRepository.findByPlacaVehiculoIn(placas);
    }

    // 3. Cambiar estado (confirmar, cancelar)
    public Reserva actualizarEstadoReserva(String id, EstadoReserva nuevoEstado, String usuarioEmail) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        Vehiculo vehiculo = vehiculoRepository.findByPlaca(reserva.getPlacaVehiculo());
        if (vehiculo == null || !vehiculo.getUsuarioEmail().equals(usuarioEmail)) {
            throw new RuntimeException("No puedes modificar reservas de vehículos que no te pertenecen.");
        }

        reserva.setEstado(nuevoEstado);
        return reservaRepository.save(reserva);
    }

    // 4. Reprogramar fecha
    public Reserva reprogramarReserva(String id, LocalDateTime nuevaFecha, String usuarioEmail) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        Vehiculo vehiculo = vehiculoRepository.findByPlaca(reserva.getPlacaVehiculo());
        if (vehiculo == null || !vehiculo.getUsuarioEmail().equals(usuarioEmail)) {
            throw new RuntimeException("No puedes reprogramar reservas de vehículos que no te pertenecen.");
        }

        reserva.setFechaReserva(nuevaFecha);
        reserva.setEstado(EstadoReserva.REPROGRAMADA);
        return reservaRepository.save(reserva);
    }

    public Reserva cambiarEstadoReserva(String idReserva, EstadoReserva nuevoEstado, String emailUsuario) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        Vehiculo vehiculo = vehiculoRepository.findByPlaca(reserva.getPlacaVehiculo());
        if (vehiculo == null || !vehiculo.getUsuarioEmail().equals(emailUsuario)) {
            throw new RuntimeException("No puedes modificar esta reserva.");
        }

        reserva.setEstado(nuevoEstado);
        return reservaRepository.save(reserva);
    }

    // Confirmar, cancelar o cambiar estado como ADMIN (sin validación de usuario)
    public Reserva cambiarEstadoComoAdmin(String id, EstadoReserva nuevoEstado) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        reserva.setEstado(nuevoEstado);
        return reservaRepository.save(reserva);
    }

    // Reprogramar como ADMIN
    public Reserva reprogramarComoAdmin(String id, LocalDateTime nuevaFecha) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        reserva.setFechaReserva(nuevaFecha);
        reserva.setEstado(EstadoReserva.REPROGRAMADA);
        return reservaRepository.save(reserva);
    }

    public List<Reserva> filtrarReservas(LocalDateTime fecha, EstadoReserva estado, String emailCliente) {
        List<Reserva> reservas = reservaRepository.findAll();

        return reservas.stream()
                .filter(r -> (fecha == null || r.getFechaReserva().toLocalDate().isEqual(fecha.toLocalDate())))
                .filter(r -> (estado == null || r.getEstado().equals(estado)))
                .filter(r -> {
                    if (emailCliente == null) return true;
                    Vehiculo vehiculo = vehiculoRepository.findByPlaca(r.getPlacaVehiculo());
                    return vehiculo != null && vehiculo.getUsuarioEmail().equals(emailCliente);
                })
                .collect(Collectors.toList());
    }





}
