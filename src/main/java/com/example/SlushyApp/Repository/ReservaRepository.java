package com.example.SlushyApp.Repository;

import com.example.SlushyApp.Model.Reserva;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaRepository extends MongoRepository<Reserva, String> {

    List<Reserva> findByPlacaVehiculo(String placaVehiculo);

    List<Reserva> findByFechaReservaBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Reserva> findByEstado(String estado);

    List<Reserva> findByPlacaVehiculoAndEstado(String placaVehiculo, String estado);

    List<Reserva> findByPlacaVehiculoIn(List<String> placas);


}
