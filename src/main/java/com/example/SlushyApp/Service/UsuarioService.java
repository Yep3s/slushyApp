package com.example.SlushyApp.Service;

import com.example.SlushyApp.Exceptions.CedulaYaRegistradaException;
import com.example.SlushyApp.Exceptions.EmailYaRegistradoException;
import com.example.SlushyApp.Model.EmpleadoRequest;
import com.example.SlushyApp.Model.Membresia;
import com.example.SlushyApp.Model.Rol;
import com.example.SlushyApp.Model.Usuario;
import com.example.SlushyApp.Repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    public Usuario registrarEmpleado(Usuario empleado) {
        if (usuarioRepository.existsByEmail(empleado.getEmail())) {
            throw new EmailYaRegistradoException("Error: El email ya está registrado.");
        }
        if (usuarioRepository.existsByCedula(empleado.getCedula())) {
            throw new CedulaYaRegistradaException("Error: La cédula ya está registrada.");
        }

        empleado.setRoles(Set.of(Rol.EMPLOYEE));
        empleado.setPassword(passwordEncoder.encode(empleado.getPassword()));
        Usuario guardado = usuarioRepository.save(empleado);

        String nombreCompleto = guardado.getNombre().trim() + " " + guardado.getApellido().trim();
        emailService.enviarCorreo(
                guardado.getEmail(),
                "¡Bienvenido a SlushyApp como empleado!",
                "Hola " + nombreCompleto + ",\n\nTu cuenta de empleado ha sido registrada exitosamente en SlushyApp."
        );
        return guardado;
    }

    public List<Usuario> obtenerUsuariosPorRol(Rol rol) {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRoles().contains(rol))
                .collect(Collectors.toList());
    }

    public Usuario actualizarEmpleado(String id, EmpleadoRequest request) {
        Usuario empleado = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        empleado.setNombre(request.getNombre());
        empleado.setApellido(request.getApellido());
        empleado.setTelefono(request.getTelefono());
        // Puedes actualizar más campos si lo deseas

        return usuarioRepository.save(empleado);
    }

    public void eliminarUsuario(String id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Empleado no encontrado");
        }
        usuarioRepository.deleteById(id);
    }

    public Page<Usuario> obtenerEmpleadosPaginados(int page, int size) {
        Pageable pg = PageRequest.of(page, size, Sort.by("fechaRegistro").descending());
        return usuarioRepository.findByRolesContains(Rol.EMPLOYEE, pg);
    }




}
