package com.example.SlushyApp.Service;

import com.example.SlushyApp.Exceptions.CedulaYaRegistradaException;
import com.example.SlushyApp.Exceptions.EmailYaRegistradoException;
import com.example.SlushyApp.Model.Rol;
import com.example.SlushyApp.Model.Usuario;
import com.example.SlushyApp.Repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;



    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    //1. Registrar Un Usuario
    public Usuario registrarUsuario(String nombre, String apellido, String email, String password, String cedula, String telefono) {
        // Verifica si el email ya está registrado
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailYaRegistradoException("Error: El email ya está registrado.");
        }

        // Verifica si la cédula ya está registrada
        if (usuarioRepository.existsByCedula(cedula)) {
            throw new CedulaYaRegistradaException("Error: La cédula ya está registrada.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password)); //Encripta la contraseña
        usuario.setCedula(cedula);
        usuario.setTelefono(telefono);
        usuario.setRoles(Set.of(Rol.USER)); // Asigna el rol "USER" por defecto

        Usuario guardado = usuarioRepository.save(usuario);

        String nombreCompleto = guardado.getNombre().trim() + " " + guardado.getApellido().trim();
        emailService.enviarCorreo(
                guardado.getEmail(),
                "¡Bienvenido a SlushyApp!",
                "Hola " + nombreCompleto + ",\n\nTu cuenta ha sido registrada exitosamente en SlushyApp. ¡Gracias por unirte! Esperamos que disfrutes de todos nuestros servicios."
        );

        return guardado;

    }

    //2. Obtener todos los usuarios
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    //3. Obtener un usuario por su ID
    public Optional<Usuario> obtenerUsuarioPorId(String id) {
        return usuarioRepository.findById(id);
    }

    //4. Cambiar el rol de un usuario
    public Usuario cambiarRolUsuario(String id, Rol nuevoRol) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setRoles(Set.of(nuevoRol)); // Se actualiza el rol
        return usuarioRepository.save(usuario);
    }

    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // Método para cambiar los roles de un usuario
    public Usuario actualizarRoles(String email, Set<Rol> nuevosRoles) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            throw new RuntimeException("Error: Usuario no encontrado.");
        }

        usuario.setRoles(nuevosRoles);
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }


}
