package com.example.SlushyApp.Service;

import com.example.SlushyApp.DTO.HistorialReservaDto;
import com.example.SlushyApp.Model.*;
import com.example.SlushyApp.Repository.PagoRepository;
import com.example.SlushyApp.Repository.ReservaRepository;
import com.example.SlushyApp.Repository.ServicioRepository;
import com.example.SlushyApp.Repository.VehiculoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    // Horario fijo de atención
    private static final LocalTime HORA_APERTURA = LocalTime.of(8, 0);
    private static final LocalTime HORA_CIERRE   = LocalTime.of(18, 0);

    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ServicioRepository servicioRepository;

    public ReservaService(PagoRepository pagoRepository, ReservaRepository reservaRepository,
                          VehiculoRepository vehiculoRepository,
                          ServicioRepository servicioRepository) {
        this.reservaRepository = reservaRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.servicioRepository = servicioRepository;
        this.pagoRepository = pagoRepository;
    }

    /** 1️⃣ Crear reserva con validaciones básicas */
    public Reserva crearReserva(Reserva reserva, String usuarioEmail) {
        // 1.1 Vehículo existe y te pertenece
        Vehiculo vehiculo = vehiculoRepository.findByPlaca(reserva.getPlacaVehiculo());
        if (vehiculo == null || !vehiculo.getUsuarioEmail().equals(usuarioEmail)) {
            throw new RuntimeException("No puedes reservar con un vehículo que no te pertenece.");
        }

        // 1.2 Fecha/hora futura
        LocalDateTime ahora = LocalDateTime.now();
        if (reserva.getFechaInicio() == null || reserva.getFechaInicio().isBefore(ahora)) {
            throw new RuntimeException("La fecha de inicio debe ser posterior al momento actual.");
        }

        // 1.3 Servicio existe
        if (!servicioRepository.existsById(reserva.getServicioId())) {
            throw new RuntimeException("El servicio seleccionado no existe.");
        }

        // 1.4 Calcular fecha fin = inicio + duración del servicio
        Servicio svc = servicioRepository.findById(reserva.getServicioId())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        LocalDateTime fin = reserva.getFechaInicio().plusMinutes(svc.getDuracionMinutos());
        reserva.setFechaFin(fin);

        // 1.5 No debe solaparse con otra reserva de ningún usuario en el mismo bloque
        List<Reserva> todas = reservaRepository.findAll();
        boolean conflict = todas.stream().anyMatch(r ->
                r.getFechaInicio().isBefore(reserva.getFechaFin()) &&
                        r.getFechaFin().isAfter(reserva.getFechaInicio())
        );
        if (conflict) {
            throw new RuntimeException("Ya existe una reserva que solapa este horario.");
        }

        // 1.6 Guardar
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setUsuarioEmail(usuarioEmail);
        reserva.setProgreso(0);
        return reservaRepository.save(reserva);
    }

    /** 2️⃣ Listar “mis reservas” por email */
    public List<Reserva> obtenerReservasPorUsuario(String email) {
        List<Vehiculo> vehs = vehiculoRepository.findByUsuarioEmail(email);
        List<String> placas = vehs.stream()
                .map(Vehiculo::getPlaca)
                .collect(Collectors.toList());
        return reservaRepository.findByPlacaVehiculoIn(placas);
    }

    /** 3️⃣ Cambiar estado (cancelar/confirmar) validando propiedad */
    public Reserva cambiarEstadoReserva(String idReserva, EstadoReserva nuevoEstado, String emailUsuario) {
        Reserva r = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        Vehiculo v = vehiculoRepository.findByPlaca(r.getPlacaVehiculo());
        if (v == null || !v.getUsuarioEmail().equals(emailUsuario)) {
            throw new RuntimeException("No puedes modificar esta reserva.");
        }
        r.setEstado(nuevoEstado);
        return reservaRepository.save(r);
    }

    /** 4️⃣ Reprogramar fecha/hora validando propiedad */
    public Reserva reprogramarReserva(String idReserva, LocalDateTime nuevaFecha, String emailUsuario) {
        Reserva r = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        Vehiculo v = vehiculoRepository.findByPlaca(r.getPlacaVehiculo());
        if (v == null || !v.getUsuarioEmail().equals(emailUsuario)) {
            throw new RuntimeException("No puedes reprogramar esta reserva.");
        }
        // calcular nuevo fin según duración del servicio
        Servicio svc = servicioRepository.findById(r.getServicioId())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        r.setFechaInicio(nuevaFecha);
        r.setFechaFin(nuevaFecha.plusMinutes(svc.getDuracionMinutos()));
        r.setEstado(EstadoReserva.REPROGRAMADA);
        return reservaRepository.save(r);
    }

    /** 5️⃣ Generar franjas libres de 8:00 a 18:00 para un servicio en un día */
    public List<LocalDateTime> obtenerSlotsDisponibles(String servicioId, LocalDate fecha) {
        Servicio svc = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        int duracion = svc.getDuracionMinutos();
        LocalDateTime inicio = fecha.atTime(HORA_APERTURA);
        LocalDateTime fin    = fecha.atTime(HORA_CIERRE);

        // Recoger **todas** las reservas de ese día (independientemente del servicio)
        List<Reserva> ocupadas = reservaRepository.findAll().stream()
                .filter(r -> r.getFechaInicio().toLocalDate().isEqual(fecha))
                .collect(Collectors.toList());

        List<LocalDateTime> slots = new ArrayList<>();
        LocalDateTime pointer = inicio;

        while (!pointer.plusMinutes(duracion).isAfter(fin)) {
            // 1) "Congelamos" el valor de pointer en final para usarlo en la lambda
            final LocalDateTime slotStart = pointer;
            final LocalDateTime slotEnd   = pointer.plusMinutes(duracion);

            // 2) Comprobamos solapamientos sobre esas variables finales
            boolean overlap = ocupadas.stream().anyMatch(r ->
                    r.getFechaInicio().isBefore(slotEnd) &&
                            r.getFechaFin().isAfter(slotStart)
            );

            if (!overlap) {
                slots.add(slotStart);
            }

            // 3) Avanzamos el puntero para el siguiente bloque
            pointer = pointer.plusMinutes(duracion);
        }

        return slots;
    }



    /** 6️⃣ Métodos “administrativos” sin validar propiedad */
    public Reserva cambiarEstadoComoAdmin(String id, EstadoReserva estado) {
        Reserva r = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        r.setEstado(estado);
        return reservaRepository.save(r);
    }

    public Reserva reprogramarComoAdmin(String id, LocalDateTime nuevaFecha) {
        Reserva r = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        Servicio svc = servicioRepository.findById(r.getServicioId())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        r.setFechaInicio(nuevaFecha);
        r.setFechaFin(nuevaFecha.plusMinutes(svc.getDuracionMinutos()));
        r.setEstado(EstadoReserva.REPROGRAMADA);
        return reservaRepository.save(r);
    }

    /** 7️⃣ Actualizar progreso de lavado */
    public Reserva actualizarProgresoReserva(String id, int nuevoProgreso) {
        if (nuevoProgreso < 0 || nuevoProgreso > 100) {
            throw new RuntimeException("El progreso debe estar entre 0 y 100.");
        }
        Reserva r = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        r.setProgreso(nuevoProgreso);
        return reservaRepository.save(r);
    }

    /** 8️⃣ Obtener reservas por estado */
    public List<Reserva> obtenerReservasPorEstado(EstadoReserva estado) {
        return reservaRepository.findAll().stream()
                .filter(r -> r.getEstado().equals(estado))
                .collect(Collectors.toList());
    }

    /** 9️⃣ Obtener todas las reservas */
    public List<Reserva> obtenerTodasLasReservas() {
        return reservaRepository.findAll();
    }

    /** 🔟 Filtrar reservas por fecha, estado o email */
    public List<Reserva> filtrarReservas(LocalDateTime fecha,
                                         EstadoReserva estado,
                                         String emailCliente) {
        return reservaRepository.findAll().stream()
                .filter(r -> fecha == null
                        || r.getFechaInicio().toLocalDate().isEqual(fecha.toLocalDate()))
                .filter(r -> estado == null
                        || r.getEstado().equals(estado))
                .filter(r -> {
                    if (emailCliente == null) return true;
                    Vehiculo v = vehiculoRepository.findByPlaca(r.getPlacaVehiculo());
                    return v != null && v.getUsuarioEmail().equals(emailCliente);
                })
                .collect(Collectors.toList());
    }

    /**
     * 🔁 Cambiar estado como EMPLEADO (sin validar propiedad)
     */
    public Reserva actualizarEstadoComoEmpleado(String id, EstadoReserva nuevoEstado) {
        Reserva r = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        r.setEstado(nuevoEstado);
        return reservaRepository.save(r);
    }

    /**
     * 🔁 Obtener por tipo de vehiculo placa
     */

    public List<Servicio> obtenerServiciosPorVehiculo(String placa) {
        Vehiculo veh = vehiculoRepository.findByPlaca(placa);
        if (veh == null) {
            throw new RuntimeException("Vehículo no encontrado: " + placa);
        }
        TipoVehiculo tipo = veh.getTipoVehiculo();

        return servicioRepository.findAll().stream()
                // sólo aquellos que tengan un precio para este tipo
                .filter(svc -> svc.getPreciosPorTipo().containsKey(tipo))
                .map(svc -> {
                    // creamos un "corte" de la entidad original,
                    // dejando sólo el precio que le interesa al cliente
                    Servicio copia = new Servicio();
                    copia.setId(svc.getId());
                    copia.setNombre(svc.getNombre());
                    copia.setDescripcion(svc.getDescripcion());
                    copia.setDuracionMinutos(svc.getDuracionMinutos());
                    copia.setEstado(svc.getEstado());
                    // { tipoVehiculo -> precio } sólo para este usuario
                    copia.setPreciosPorTipo(Map.of(tipo, svc.getPreciosPorTipo().get(tipo)));
                    return copia;
                })
                .collect(Collectors.toList());
    }

    public Pago obtenerPagoPorReserva(String reservaId) {
        return pagoRepository.findByReservaId(reservaId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado para reserva " + reservaId));
    }

    public Pago simularPago(Reserva reserva) {
        Servicio svc = servicioRepository.findById(reserva.getServicioId())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        Vehiculo veh = vehiculoRepository.findByPlaca(reserva.getPlacaVehiculo());

        TipoVehiculo tipo = veh.getTipoVehiculo();
        Double precio = svc.getPreciosPorTipo().get(tipo);
        if (precio == null) {
            throw new RuntimeException("No hay precio para tipo " + tipo);
        }

        Pago pago = new Pago();
        pago.setReservaId(reserva.getId());
        pago.setMonto(precio);
        pago.setFechaPago(LocalDateTime.now());
        pago.setStatus(PaymentStatus.COMPLETED);   // coincide con tu campo `status`
        pago.setMetodo(PaymentMethod.SIMULATED);   // método de pago simulado

        return pagoRepository.save(pago);
    }

    public List<HistorialReservaDto> obtenerHistorial(String emailUsuario) {
        // 1️⃣ traer todas las reservas del usuario
        List<Reserva> reservas = obtenerReservasPorUsuario(emailUsuario);
        // 2️⃣ mapear cada reserva a nuestro DTO
        return reservas.stream()
                .sorted(Comparator.comparing(Reserva::getFechaInicio).reversed())
                .map(r -> {
                    Servicio svc = servicioRepository.findById(r.getServicioId())
                            .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
                    Pago pago = pagoRepository.findByReservaId(r.getId()).orElse(null);

                    HistorialReservaDto dto = new HistorialReservaDto();
                    dto.setReservaId(r.getId());
                    dto.setServicioNombre(svc.getNombre());
                    dto.setFechaInicio(r.getFechaInicio());
                    dto.setEstado(r.getEstado().name());
                    dto.setPlacaVehiculo(r.getPlacaVehiculo());
                    dto.setMonto(pago != null ? pago.getMonto() : 0);
                    return dto;
                })
                .collect(Collectors.toList());
    }



}
