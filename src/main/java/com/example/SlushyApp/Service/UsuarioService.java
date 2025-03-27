package com.example.SlushyApp.Service;

import com.example.SlushyApp.Model.Usuario;
import com.example.SlushyApp.Repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrarUsuario(String nombre, String apellido, String email, String password, String cedula, String telefono) {
        // Verifica si el usuario ya está registrado
        Optional<Usuario> usuarioExistente = Optional.ofNullable(usuarioRepository.findByEmail(email));
        if (usuarioExistente.isPresent()) {
            throw new RuntimeException("Error: El email ya está registrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password)); //Encripta la contraseña
        usuario.setCedula(cedula);
        usuario.setTelefono(telefono);
        usuario.setRoles(Collections.singletonList("USER")); // Asigna el rol "USER" por defecto

        return usuarioRepository.save(usuario);
    }

    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }


}
