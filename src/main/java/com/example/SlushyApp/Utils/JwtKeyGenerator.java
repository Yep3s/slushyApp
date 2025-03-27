package com.example.SlushyApp.Utils;

import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.security.Key;

public class JwtKeyGenerator {
    public static void main(String[] args) {
        Key key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        String secretKey = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println("Clave segura: " + secretKey);
    }
}
