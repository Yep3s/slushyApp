package com.example.SlushyApp.Repository;

import com.example.SlushyApp.Model.EstadoReserva;
import com.example.SlushyApp.Model.Reserva;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservaRepository extends MongoRepository<Reserva, String> {

    long countByFechaInicioBetween(LocalDateTime desde, LocalDateTime hasta);

    // — Métodos existentes adaptados a la nueva nomenclatura — //

    List<Reserva> findByPlacaVehiculo(String placaVehiculo);

    /** Antes: findByFechaReservaBetween */
    List<Reserva> findByFechaInicioBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Reserva> findByEstado(String estado);

    List<Reserva> findByPlacaVehiculoAndEstado(String placaVehiculo, String estado);

    List<Reserva> findByPlacaVehiculoIn(List<String> placas);

    /** Antes: findByUsuarioEmailOrderByFechaReservaDesc */
    List<Reserva> findByUsuarioEmailOrderByFechaInicioDesc(String email);
    
    // — Nuevo: detección de solapamientos — //
    /**
     * Busca todas las reservas que arranquen antes de 'fechaFin'
     * y terminen después de 'fechaInicio' → detecta cualquier cruce de franjas.
     */
    List<Reserva> findByFechaInicioLessThanAndFechaFinGreaterThan(
            LocalDateTime fechaFin, LocalDateTime fechaInicio
    );

    List<Reserva> findByUsuarioEmailAndEstado(String usuarioEmail, EstadoReserva estado);



}
