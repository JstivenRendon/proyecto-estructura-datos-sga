package com.sga.excepciones;

public class EstructuraVaciaException extends Exception {
    private static final long serialVersionUID = 1L;

    public EstructuraVaciaException(String mensaje) {
        super(mensaje);
    }
}
