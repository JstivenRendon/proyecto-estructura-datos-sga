package modelo;

import java.time.LocalTime;

import estructuras.ArbolBinarioBusqueda;

public class Apartamento implements Comparable<Apartamento> {
    private final String codigo;
    private String nombre;
    private String bloque;
    private int piso;
    private int numeroDormitorios;
    private int capacidadMaxima;
    private String caracteristicas;
    private double tarifaBasePorPersona;
    private EstadoOperativo estadoOperativo;
    private String motivoFueraDeServicio;
    private LocalTime horaLlegadaProgramadaHoy;
    private final ArbolBinarioBusqueda<Reserva> reservas = new ArbolBinarioBusqueda<>();

    public Apartamento(String codigo, String nombre, String bloque, int piso,
            int numeroDormitorios, int capacidadMaxima, String caracteristicas,
            double tarifaBasePorPersona, EstadoOperativo estadoOperativo,
            String motivoFueraDeServicio, LocalTime horaLlegadaProgramadaHoy) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.bloque = bloque;
        this.piso = piso;
        this.numeroDormitorios = numeroDormitorios;
        this.capacidadMaxima = capacidadMaxima;
        this.caracteristicas = caracteristicas;
        this.tarifaBasePorPersona = tarifaBasePorPersona;
        this.estadoOperativo = estadoOperativo;
        this.motivoFueraDeServicio = motivoFueraDeServicio;
        this.horaLlegadaProgramadaHoy = horaLlegadaProgramadaHoy;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getBloque() {
        return bloque;
    }

    public int getPiso() {
        return piso;
    }

    public int getNumeroDormitorios() {
        return numeroDormitorios;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public String getCaracteristicas() {
        return caracteristicas;
    }

    public double getTarifaBasePorPersona() {
        return tarifaBasePorPersona;
    }

    public EstadoOperativo getEstadoOperativo() {
        return estadoOperativo;
    }

    public String getMotivoFueraDeServicio() {
        return motivoFueraDeServicio;
    }

    public LocalTime getHoraLlegadaProgramadaHoy() {
        return horaLlegadaProgramadaHoy;
    }

    public ArbolBinarioBusqueda<Reserva> getReservas() {
        return reservas;
    }

    @Override
    public int compareTo(Apartamento otro) {
        return codigo.compareTo(otro.codigo);
    }

    @Override
    public String toString() {
        return codigo;
    }
}
