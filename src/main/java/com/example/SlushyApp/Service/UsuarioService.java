package com.example.SlushyApp.Service;

import com.example.SlushyApp.DTO.ProfileUpdateDto;
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
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }


    // 1. Registrar un usuario normal (rol USER)
    public Usuario registrarUsuario(String nombre, String apellido, String email, String password, String cedula, String telefono) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailYaRegistradoException("Error: El email ya está registrado.");
        }
        if (usuarioRepository.existsByCedula(cedula)) {
            throw new CedulaYaRegistradaException("Error: La cédula ya está registrada.");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setCedula(cedula);
        usuario.setTelefono(telefono);
        usuario.setRoles(Set.of(Rol.USER));
        Usuario guardado = usuarioRepository.save(usuario);

        String nombreCompleto = guardado.getNombre().trim() + " " + guardado.getApellido().trim();
        emailService.enviarCorreo(
                guardado.getEmail(),
                "¡Bienvenido a SlushyApp!",
                "Hola " + nombreCompleto + ",\n\nTu cuenta ha sido registrada exitosamente en SlushyApp. ¡Gracias por unirte!"
        );
        return guardado;
    }

    // 2. Registrar empleado (rol EMPLOYEE)
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

    // 3. Obtener todos los usuarios (sin filtrar)
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    // 4. Obtener usuario por ID
    public Optional<Usuario> obtenerUsuarioPorId(String id) {
        return usuarioRepository.findById(id);
    }

    // Obtener usuario por ROL
    public List<Usuario> obtenerUsuariosPorRol(Rol rol) {
        return usuarioRepository.findByRolesContains(rol);
    }

    // 5. Cambiar rol de usuario
    public Usuario cambiarRolUsuario(String id, Rol nuevoRol) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setRoles(Set.of(nuevoRol));
        return usuarioRepository.save(usuario);
    }

    // 6. Actualizar datos generales de usuario
    public Usuario actualizarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // 7. Actualizar empleado específico
    public Usuario actualizarEmpleado(String id, EmpleadoRequest request) {
        Usuario empleado = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        empleado.setNombre(request.getNombre());
        empleado.setApellido(request.getApellido());
        empleado.setTelefono(request.getTelefono());
        return usuarioRepository.save(empleado);
    }

    // 8. Eliminar usuario
    public void eliminarUsuario(String id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }

    // 9. Paginado de empleados (rol EMPLOYEE)
    public Page<Usuario> obtenerEmpleadosPaginados(int page, int size) {
        Pageable pg = PageRequest.of(page, size, Sort.by("fechaRegistro").descending());
        return usuarioRepository.findByRolesContains(Rol.EMPLOYEE, pg);
    }

    // ------- Métodos para clientes (rol USER) -------

    /**
     * Devuelve todos los clientes (rol USER) sin paginar.
     */
    public List<Usuario> obtenerClientes() {
        return usuarioRepository.findByRolesContains(Rol.USER, Pageable.unpaged()).getContent();
    }

    /**
     * Paginado de clientes (rol USER).
     */
    public Page<Usuario> obtenerClientesPaginados(int page, int size, String membresiaFilter) {
        Pageable pg = PageRequest.of(page, size, Sort.by("fechaRegistro").descending());
        if (membresiaFilter == null || membresiaFilter.isBlank()) {
            return usuarioRepository.findByRolesContains(Rol.USER, pg);
        }
        Membresia m = Membresia.valueOf(membresiaFilter.toUpperCase());
        return usuarioRepository.findByRolesContainsAndMembresia(Rol.USER, m, pg);
    }

    /**
     * Cuenta total de clientes (rol USER).
     */
    public long countTotalClientes() {
        return usuarioRepository.countByRolesContains(Rol.USER);
    }

    /**
     * Cuenta clientes VIP.
     */
    public long countClientesVip() {
        return usuarioRepository.countByRolesContainsAndMembresia(Rol.USER, Membresia.VIP);
    }

    /**
     * Cuenta clientes Standard.
     */
    public long countClientesStandard() {
        return usuarioRepository.countByRolesContainsAndMembresia(Rol.USER, Membresia.STANDARD);
    }

    /**
     * Cuenta clientes nuevos en los últimos N días.
     */
    public long countClientesNuevos(int dias) {
        LocalDate desde = LocalDate.now().minusDays(dias);
        return usuarioRepository.countByRolesContainsAndFechaRegistroAfter(Rol.USER, desde);
    }

    //camio contraseñas y actualizar perfil

    public Usuario updateProfile(String email, ProfileUpdateDto dto) {
        Usuario u = usuarioRepository.findByEmail(email);
        if (u == null) throw new RuntimeException("Usuario no encontrado");
        u.setNombre(dto.getNombre());
        u.setApellido(dto.getApellido());
        u.setCedula(dto.getCedula());
        u.setTelefono(dto.getTelefono());
        return usuarioRepository.save(u);
    }

    public void changePassword(String email, String current, String nuevo, String confirm) {
        Usuario u = usuarioRepository.findByEmail(email);
        if (!passwordEncoder.matches(current, u.getPassword())) {
            throw new RuntimeException("La contraseña actual no coincide.");
        }
        if (!nuevo.equals(confirm)) {
            throw new RuntimeException("La nueva contraseña y su confirmación no coinciden.");
        }
        u.setPassword(passwordEncoder.encode(nuevo));
        usuarioRepository.save(u);
    }


}
