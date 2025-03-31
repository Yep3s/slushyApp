package com.example.SlushyApp.Service;

import com.example.SlushyApp.Model.PasswordResetToken;
import com.example.SlushyApp.Repository.PasswordResetTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public PasswordResetToken generarToken(String email) {
        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(email);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiration(LocalDateTime.now().plusMinutes(30)); // Token válido 30 minutos
        return tokenRepository.save(token);
    }

    public PasswordResetToken validarToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpiration().isBefore(LocalDateTime.now())) {
            return null;
        }
        return resetToken;
    }

    public void eliminarToken(String token) {
        PasswordResetToken t = tokenRepository.findByToken(token);
        if (t != null) tokenRepository.delete(t);
    }

}
