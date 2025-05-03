package com.example.SlushyApp.Repository;

import com.example.SlushyApp.Model.Membresia;
import com.example.SlushyApp.Model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario,String> {

    Usuario findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCedula(String cedula);

    long countByMembresia(Membresia membresia);

    long countByFechaRegistroAfter(LocalDate date);


}
