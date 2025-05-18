package com.example.SlushyApp.Exceptions;

public class SlotNotAvailableException extends RuntimeException{
    public SlotNotAvailableException(String mensaje) {
        super(mensaje);
    }
}
