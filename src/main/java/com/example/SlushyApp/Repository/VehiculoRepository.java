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

    //cosas nuevas

    // 🔍 Buscar por placa que contenga una cadena (insensible a mayúsculas)
    List<Vehiculo> findByPlacaContainingIgnoreCase(String placa);

    // 🔍 Filtrar por tipo de vehículo
    List<Vehiculo> findByTipoVehiculo(String tipoVehiculo);

    List<Vehiculo> findByPlacaContainingIgnoreCaseOrUsuarioEmailContainingIgnoreCase(String placa, String email);


}
