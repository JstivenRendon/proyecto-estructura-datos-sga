package com.sga.modelo;

import java.time.LocalDate;

public class Huesped implements Comparable<Huesped> {
    private final String nombre;
    private final String documento;
    private final LocalDate fechaNacimiento;
    private final String telefono;
    private final String correo;
    private final String rutaFotografia;

    public Huesped(String nombre, String documento, LocalDate fechaNacimiento,
            String telefono, String correo, String rutaFotografia) {
        this.nombre = nombre;
        this.documento = documento;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.correo = correo;
        this.rutaFotografia = rutaFotografia;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public String getRutaFotografia() {
        return rutaFotografia;
    }

    // TODO Integración Persona A: Conjunto<Apartamento> favoritos y ListaDoblementeEnlazada<Reserva> historial

    @Override
    public int compareTo(Huesped otro) {
        return documento.compareTo(otro.documento);
    }
}
