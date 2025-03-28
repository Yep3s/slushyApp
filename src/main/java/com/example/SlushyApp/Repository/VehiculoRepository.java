package com.example.SlushyApp.Repository;

import com.example.SlushyApp.Model.Vehiculo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehiculoRepository extends MongoRepository<Vehiculo, String> {

    List<Vehiculo> findByUsuarioEmail(String usuarioEmail);

    boolean existsByPlaca(String placa);

    Vehiculo findByPlaca(String placa);


}
