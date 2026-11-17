package modelo;

import java.time.LocalDate;

import estructuras.Conjunto;
import estructuras.ListaEnlazada;

public class Huesped implements Comparable<Huesped> {
    private final String nombre;
    private final String documento;
    private final LocalDate fechaNacimiento;
    private final String telefono;
    private final String correo;
    private final String rutaFotografia;
    private final Conjunto<String> favoritos = new Conjunto<>();
    private final ListaEnlazada<Object> historial = new ListaEnlazada<>();

    public Huesped(String documento, String nombre, LocalDate fechaNacimiento,
                   String telefono, String correo) {
        this(nombre, documento, fechaNacimiento, telefono, correo, null);
    }

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

    public boolean agregarFavorito(String codigoApartamento) {
        return favoritos.agregar(codigoApartamento);
    }

    public void agregarAlHistorial(Object item) {
        historial.agregarFinal(item);
    }

    public Conjunto<String> getFavoritos() {
        return favoritos;
    }

    public ListaEnlazada<Object> getHistorial() {
        return historial;
    }

    @Override
    public int compareTo(Huesped otro) {
        return documento.compareTo(otro.documento);
    }
}