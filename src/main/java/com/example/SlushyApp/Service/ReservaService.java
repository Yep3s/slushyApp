package com.example.SlushyApp.Service;

import com.example.SlushyApp.Model.EstadoReserva;
import com.example.SlushyApp.Model.Reserva;
import com.example.SlushyApp.Model.Vehiculo;
import com.example.SlushyApp.Repository.ReservaRepository;
import com.example.SlushyApp.Repository.ServicioRepository;
import com.example.SlushyApp.Repository.VehiculoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ServicioRepository servicioRepository;

    public ReservaService(ReservaRepository reservaRepository,
                          VehiculoRepository vehiculoRepository,
                          ServicioRepository servicioRepository) {
        this.reservaRepository = reservaRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.servicioRepository = servicioRepository;
    }

    // 1. Crear reserva con validaciones
    public Reserva crearReserva(Reserva reserva, String usuarioEmail) {
        Vehiculo vehiculo = vehiculoRepository.findByPlaca(reserva.getPlacaVehiculo());
        if (vehiculo == null || !vehiculo.getUsuarioEmail().equals(usuarioEmail)) {
            throw new RuntimeException("No puedes reservar con un vehículo que no te pertenece.");
        }

        // Validar que la fecha es posterior al momento actual
        LocalDateTime ahora = LocalDateTime.now();
        if (reserva.getFechaReserva() == null || reserva.getFechaReserva().isBefore(ahora)) {
            throw new RuntimeException("La fecha y hora de la reserva deben ser posteriores al momento actual.");
        }

        // Validar que no exista otra reserva en la misma fecha y hora exacta
        List<Reserva> reservasExistentes = reservaRepository.findAll();
        boolean yaReservada = reservasExistentes.stream()
                .anyMatch(r -> r.getFechaReserva().isEqual(reserva.getFechaReserva()));
        if (yaReservada) {
            throw new RuntimeException("Ya existe una reserva para esa fecha y hora. Por favor elige otro horario.");
        }

        // Validar que el servicio exista
        if (!servicioRepository.existsByNombre(reserva.getServicioNombre())) {
            throw new RuntimeException("El servicio seleccionado no existe.");
        }

        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setUsuarioEmail(usuarioEmail);
        reserva.setProgreso(0); // Inicializa el progreso en 0%
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

    // 🔁 Cambiar estado como empleado (sin validación por usuario)
    public Reserva actualizarEstadoComoEmpleado(String id, EstadoReserva nuevoEstado) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        reserva.setEstado(nuevoEstado);
        return reservaRepository.save(reserva);
    }

    // 📋 Obtener reservas por estado
    public List<Reserva> obtenerReservasPorEstado(EstadoReserva estado) {
        return reservaRepository.findAll()
                .stream()
                .filter(r -> r.getEstado().equals(estado))
                .collect(Collectors.toList());
    }

    public List<Reserva> obtenerTodasLasReservas() {
        return reservaRepository.findAll();
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

    public Reserva actualizarProgresoReserva(String id, int nuevoProgreso) {
        if (nuevoProgreso < 0 || nuevoProgreso > 100) {
            throw new RuntimeException("El progreso debe estar entre 0 y 100.");
        }

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        reserva.setProgreso(nuevoProgreso);
        return reservaRepository.save(reserva);
    }

}