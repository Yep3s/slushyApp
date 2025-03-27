package com.example.SlushyApp.Controller;

import com.example.SlushyApp.Model.LoginRequest;
import com.example.SlushyApp.Model.Usuario;
import com.example.SlushyApp.Service.UsuarioService;
import com.example.SlushyApp.Utils.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioService usuarioService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        Usuario usuario = usuarioService.findByEmail(loginRequest.getEmail());

        if (usuario == null || !passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(usuario);
        return ResponseEntity.ok(token);
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
    public ResponseEntity<String> registrarUsuario(@Valid @RequestBody Usuario usuario) {
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
