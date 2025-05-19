package com.example.SlushyApp.Controller;

import com.example.SlushyApp.DTO.PasswordChangeDto;
import com.example.SlushyApp.DTO.ProfileUpdateDto;
import com.example.SlushyApp.Model.LoginRequest;
import com.example.SlushyApp.Model.PasswordResetToken;
import com.example.SlushyApp.Model.Usuario;
import com.example.SlushyApp.Service.EmailService;
import com.example.SlushyApp.Service.PasswordResetService;
import com.example.SlushyApp.Service.UsuarioService;
import com.example.SlushyApp.Utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PasswordResetService passwordResetService;

    public AuthController(
            UsuarioService usuarioService,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            PasswordResetService passwordResetService) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        Usuario usuario = usuarioService.findByEmail(loginRequest.getEmail());

        if (usuario == null || !passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(usuario);

        // Crear la cookie segura
        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(true) // solo HTTPS en producción
                .path("/")
                .maxAge(24 * 60 * 60) // 1 día
                .sameSite("Strict") // o "Lax"
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        // También puedes enviar el rol como info útil para redirigir desde frontend
        String rol = usuario.getRoles().stream().findFirst().orElseThrow().name();
        return ResponseEntity.ok(Map.of("rol", rol));
    }

    @PostMapping("/register")
    public ResponseEntity<String> registrarUsuario(
            @RequestParam String nombre,
            @RequestParam String apellido,
          @RequestParam String email,
          @RequestParam String password,
            @RequestParam String cedula,
            @RequestParam String telefono) {
        usuarioService.registrarUsuario(nombre, apellido, email, password, cedula, telefono);
        return ResponseEntity.ok("Usuario Registrado Con Exito");
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(0) // Expira la cookie
                .sameSite("Strict")
                .build();

        response.setHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    public ResponseEntity<Usuario> obtenerUsuario(@RequestParam String email) {
        return ResponseEntity.ok(usuarioService.findByEmail(email));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> solicitarRestablecimiento(@RequestParam String email) {
        Usuario usuario = usuarioService.findByEmail(email);
        if (usuario == null) {
            return ResponseEntity.badRequest().body("El email no está registrado.");
        }

        PasswordResetToken token = passwordResetService.generarToken(email);
        String enlace = "http://localhost:8080/auth/reset-password?token=" + token.getToken();

        emailService.enviarCorreo(email,
                "Restablecer tu contraseña - SlushyApp",
                "Hola " + usuario.getNombre().trim() + " " + usuario.getApellido().trim() +
                        ",\n\nRecibimos una solicitud para restablecer tu contraseña. Haz clic en el siguiente enlace para continuar:\n\n" +
                        enlace + "\n\nEste enlace expirará en 30 minutos.");

        return ResponseEntity.ok("Correo enviado con instrucciones para restablecer tu contraseña.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> restablecerContrasena(
            @RequestParam String token,
            @RequestParam String nuevaContrasena) {

        PasswordResetToken resetToken = passwordResetService.validarToken(token);
        if (resetToken == null) {
            return ResponseEntity.badRequest().body("El token es inválido o ha expirado.");
        }

        Usuario usuario = usuarioService.findByEmail(resetToken.getEmail());
        if (usuario == null) {
            return ResponseEntity.badRequest().body("Usuario no encontrado.");
        }

        usuario.setPassword(passwordEncoder.encode(nuevaContrasena));
        usuarioService.actualizarUsuario(usuario);
        passwordResetService.eliminarToken(token);

        // 📧 Enviar confirmación
        emailService.enviarCorreo(
                usuario.getEmail(),
                "Contraseña restablecida con éxito",
                "Hola " + usuario.getNombre().trim() + " " + usuario.getApellido().trim() + ",\n\n" +
                        "Tu contraseña ha sido restablecida correctamente. Si tú no realizaste esta acción, por favor comunícate con el equipo de soporte de SlushyApp inmediatamente."
        );

        return ResponseEntity.ok("Contraseña restablecida correctamente.");

    }

    @GetMapping("/me")
    public ResponseEntity<Usuario> obtenerUsuarioDesdeToken(HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromCookie(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = jwtUtil.getEmailFromToken(token);
        Usuario usuario = usuarioService.findByEmail(email);
        return ResponseEntity.ok(usuario);
    }

    /** ✏️ Actualiza solo nombre, apellido, cédula y teléfono */
    @PutMapping("/me")
    public ResponseEntity<Usuario> updateProfile(
            @Valid @RequestBody ProfileUpdateDto dto,
            HttpServletRequest req
    ) {
        String token = jwtUtil.extractTokenFromCookie(req);
        String email = jwtUtil.getEmailFromToken(token);
        Usuario updated = usuarioService.updateProfile(email, dto);
        return ResponseEntity.ok(updated);
    }

    // 👉 Cambiar contraseña
    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody PasswordChangeDto dto,
            HttpServletRequest request
    ) {
        String token = jwtUtil.extractTokenFromCookie(request);
        String email = jwtUtil.getEmailFromToken(token);
        usuarioService.changePassword(email,
                dto.getCurrentPassword(),
                dto.getNewPassword(),
                dto.getConfirmPassword()
        );
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }


}
