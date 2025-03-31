package com.example.SlushyApp.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "PasswordResetTokens")
public class PasswordResetToken {

    @Id
    private String id;
    private String email;
    private String token;
    private LocalDateTime expiration;

    public PasswordResetToken() {
    }

    public PasswordResetToken(String id, String email, String token, LocalDateTime expiration) {
        this.id = id;
        this.email = email;
        this.token = token;
        this.expiration = expiration;
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDateTime expiration) {
        this.expiration = expiration;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
