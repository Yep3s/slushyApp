package com.example.SlushyApp.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailYaRegistradoException.class)
    public ResponseEntity<String> handleEmailYaRegistradoException(EmailYaRegistradoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(CedulaYaRegistradaException.class)
    public ResponseEntity<String> handleCedulaYaRegistradaException(CedulaYaRegistradaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(PlacaYaRegistradaException.class)
    public ResponseEntity<String> handlePlacaYaRegistradaException(PlacaYaRegistradaException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleEnumConversionError(HttpMessageNotReadableException ex) {
        if (ex.getMessage().contains("TipoVehiculo")) {
            return ResponseEntity.badRequest().body("Tipo de vehículo no válido. Debe ser AUTOMOVIL, CAMIONETA o MOTO.");
        }
        return ResponseEntity.badRequest().body("Error de formato en la solicitud.");
    }

}
