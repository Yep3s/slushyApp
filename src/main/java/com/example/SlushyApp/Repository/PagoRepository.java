package com.example.SlushyApp.Repository;

import com.example.SlushyApp.Model.Pago;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PagoRepository extends MongoRepository<Pago, String> {

    Optional<Pago> findByReservaId(String reservaId);

    @Query("{$match: {fechaPago: {$gte: ?0, $lt: ?1}}}, {$group:{_id:null, total:{$sum:'$monto'}}}")
    Double sumMontoByFechaPagoBetween(LocalDateTime desde, LocalDateTime hasta);

}
