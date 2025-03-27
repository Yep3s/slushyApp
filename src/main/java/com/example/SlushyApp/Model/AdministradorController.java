package com.example.SlushyApp.Model;

import com.example.SlushyApp.Service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdministradorController {

    private final UsuarioService usuarioService;

    public AdministradorController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }


    // 1. Listar todos los usuarios
    @GetMapping("/lista")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    // 2. Obtener detalles de un usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable String id) {
        Optional<Usuario> usuario = usuarioService.obtenerUsuarioPorId(id);
        return usuario.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 3. Cambiar rol de un usuario
    @PutMapping("/cambiar-rol/{id}")
    public ResponseEntity<Usuario> cambiarRolUsuario(@PathVariable String id, @RequestParam String nuevoRol) {
        try {
            Rol rolEnum = Rol.valueOf(nuevoRol.toUpperCase()); // Convertimos el String a Enum
            Usuario usuarioActualizado = usuarioService.cambiarRolUsuario(id, rolEnum);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null); // Si el rol no es válido, retorna 400 Bad Request
        }
    }

}
