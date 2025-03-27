package com.example.SlushyApp.Controller;

import com.example.SlushyApp.Model.Usuario;
import com.example.SlushyApp.Service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registrarUsuario(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String cedula,
            @RequestParam String telefono){
        usuarioService.registrarUsuario(nombre, apellido, email, password, cedula, telefono);
        return ResponseEntity.ok("Usuario Registrado Con Exito");
    }

    @PostMapping("/register1")
    public ResponseEntity<String> registrarUsuario(@RequestBody Usuario usuario) {
        usuarioService.registrarUsuario(
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getPassword(),
                usuario.getCedula(),
                usuario.getTelefono()
        );
        return ResponseEntity.ok("Usuario Registrado Con Exito");
    }

    @GetMapping("/users")
    public ResponseEntity<Usuario> obtenerUsuario(@RequestParam String email){
        return ResponseEntity.ok(usuarioService.findByEmail(email));
    }

}
