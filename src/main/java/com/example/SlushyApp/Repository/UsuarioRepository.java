package com.example.SlushyApp.Repository;

import com.example.SlushyApp.Model.Membresia;
import com.example.SlushyApp.Model.Rol;
import com.example.SlushyApp.Model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario,String> {

    long countByFechaRegistroBetween(LocalDateTime desde, LocalDateTime hasta);

    Usuario findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCedula(String cedula);

    long countByMembresia(Membresia membresia);

    long countByFechaRegistroAfter(LocalDate date);

    Page<Usuario> findByMembresia(Membresia membresia, Pageable pageable);

    Page<Usuario> findByRolesContains(Rol rol, Pageable pageable);

    /** Cuenta todos los usuarios que tienen el rol dado. */
    long countByRolesContains(Rol rol);

    /** Cuenta usuarios de un rol y membresía específica (VIP, STANDARD). */
    long countByRolesContainsAndMembresia(Rol rol, Membresia membresia);

    /** Para “clientes nuevos”: cuenta usuarios con rol X y fechaRegistro posterior a la fecha dada. */
    long countByRolesContainsAndFechaRegistroAfter(Rol rol, LocalDate fecha);

    /** Paginado de usuarios que tienen el rol dado y optionally filtrar por membresía. */
    Page<Usuario> findByRolesContainsAndMembresia(Rol rol, Membresia membresia, Pageable pageable);

    List<Usuario> findByRolesContains(Rol rol);



}
