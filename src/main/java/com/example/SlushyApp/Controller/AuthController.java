package com.example.SlushyApp.Controller;

import com.example.SlushyApp.Model.LoginRequest;
import com.example.SlushyApp.Model.PasswordResetToken;
import com.example.SlushyApp.Model.Usuario;
import com.example.SlushyApp.Service.EmailService;
import com.example.SlushyApp.Service.PasswordResetService;
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
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        Usuario usuario = usuarioService.findByEmail(loginRequest.getEmail());

        if (usuario == null || !passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(usuario);
        return ResponseEntity.ok(token);
    }

//    @PostMapping("/register")
 //   public ResponseEntity<String> registrarUsuario(
   //         @RequestParam String nombre,
    //        @RequestParam String apellido,
      //    @RequestParam String password,
        //    @RequestParam String cedula,
          //  @RequestParam String telefono) {
       // usuarioService.registrarUsuario(nombre, apellido, email, password, cedula, telefono);
       // return ResponseEntity.ok("Usuario Registrado Con Exito");
   // }

    @PostMapping("/register1")
    public ResponseEntity<String> registrarUsuario(@RequestBody Usuario usuario) {
        Usuario guardado = usuarioService.registrarUsuario(
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getPassword(),
                usuario.getCedula(),
                usuario.getTelefono()
        );

        return ResponseEntity.ok("Usuario Registrado Con Éxito");
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
}
