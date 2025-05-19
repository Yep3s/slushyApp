package com.example.SlushyApp.Repository;

import com.example.SlushyApp.Model.Pago;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PagoRepository extends MongoRepository<Pago, String> {

    Optional<Pago> findByReservaId(String reservaId);

}
