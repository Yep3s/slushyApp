package com.example.SlushyApp.Controller;

import com.example.SlushyApp.Model.EmpleadoRequest;
import com.example.SlushyApp.Model.Rol;
import com.example.SlushyApp.Model.Usuario;
import com.example.SlushyApp.Service.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/empleados")
public class EmpleadoAdminController {

    private final UsuarioService usuarioService;

    public EmpleadoAdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/crear")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Usuario> registrarEmpleado(@RequestBody Usuario empleado) {
        Usuario nuevoEmpleado = usuarioService.registrarEmpleado(empleado);
        return ResponseEntity.status(201).body(nuevoEmpleado);
    }

    // Obtener todos los empleados
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<Usuario>> listarEmpleados() {
        return ResponseEntity.ok(usuarioService.obtenerUsuariosPorRol(Rol.EMPLOYEE));
    }

    // Actualizar datos de un empleado
    @PutMapping("/actualizar/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Usuario> actualizarEmpleado(
            @PathVariable String id,
            @RequestBody EmpleadoRequest request
    ) {
        Usuario actualizado = usuarioService.actualizarEmpleado(id, request);
        return ResponseEntity.ok(actualizado);
    }

    // Eliminar un empleado
    @DeleteMapping("/eliminar/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> eliminarEmpleado(@PathVariable String id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.ok("Empleado eliminado correctamente");
    }

    // GET paginado
    @GetMapping
    public Page<Usuario> listarEmpleados(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="5") int size
    ) {
        return usuarioService.obtenerEmpleadosPaginados(page, size);
    }

}
