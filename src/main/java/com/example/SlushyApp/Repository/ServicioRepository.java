package com.example.SlushyApp.Repository;

import com.example.SlushyApp.Model.Servicio;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicioRepository extends MongoRepository<Servicio, String> {
    boolean existsByNombre(String nombre);
}
