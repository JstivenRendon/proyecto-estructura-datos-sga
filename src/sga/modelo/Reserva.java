package modelo;

import java.time.LocalDateTime;

public class Reserva implements Comparable<Reserva> {
    private final String idReserva;
    private final Huesped titular;
    private final Apartamento apartamento;
    private final Estancia estancia;
    private final CanalReserva canal;
    private final EstadoReserva estado;
    private final LocalDateTime fechaCreacion;
    private final LocalDateTime fechaVencimiento;
    private final int cantidadOcupantes;

    public Reserva(String idReserva, Huesped titular, Apartamento apartamento,
            Estancia estancia, CanalReserva canal, EstadoReserva estado,
            LocalDateTime fechaCreacion, int cantidadOcupantes) {
        this.idReserva = idReserva;
        this.titular = titular;
        this.apartamento = apartamento;
        this.estancia = estancia;
        this.canal = canal;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.fechaVencimiento = estado == EstadoReserva.PENDIENTE && fechaCreacion != null
                ? fechaCreacion.plusHours(24) : null;
        this.cantidadOcupantes = cantidadOcupantes;
    }

    public String getIdReserva() {
        return idReserva;
    }

    public Huesped getTitular() {
        return titular;
    }

    public Apartamento getApartamento() {
        return apartamento;
    }

    public Estancia getEstancia() {
        return estancia;
    }

    public CanalReserva getCanal() {
        return canal;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaVencimiento() {
        return fechaVencimiento;
    }

    public int getCantidadOcupantes() {
        return cantidadOcupantes;
    }

    // TODO Integración Persona A: ListaDoblementeEnlazada<Ocupante> ocupantes y Pila<MovimientoFolio> folio

    @Override
    public int compareTo(Reserva otra) {
        return estancia.getFechaEntrada().compareTo(otra.estancia.getFechaEntrada());
    }
}
