package modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import excepciones.TransicionEstadoInvalidaException;

public class Reserva implements Comparable<Reserva> {
    private final String idReserva;
    private final Huesped titular;
    private final Apartamento apartamento;
    private final Estancia estancia;
    private final CanalReserva canal;
    private EstadoReserva estado;
    private final LocalDateTime fechaCreacion;
    private final LocalDateTime fechaVencimiento;
    private final int cantidadOcupantes;
    private final Folio folio = new Folio();

    public Reserva(Huesped titular, Apartamento apartamento, Estancia estancia, CanalReserva canal) {
        this(null, titular, apartamento, estancia, canal, EstadoReserva.PENDIENTE, LocalDateTime.now(), 1);
    }

    public Reserva(String idReserva, Huesped titular, Apartamento apartamento,
            Estancia estancia, CanalReserva canal, EstadoReserva estado,
            LocalDateTime fechaCreacion, int cantidadOcupantes) {
        this.idReserva = idReserva;
        this.titular = titular;
        this.apartamento = apartamento;
        this.estancia = estancia;
        this.canal = canal;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion == null ? LocalDateTime.now() : fechaCreacion;
        this.fechaVencimiento = estado == EstadoReserva.PENDIENTE
                ? this.fechaCreacion.plusHours(24) : this.fechaCreacion;
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

    public Folio getFolio() {
        return folio;
    }

    public LocalDateTime getVenceEn() {
        return fechaVencimiento;
    }

    public int personasQuePagan(LocalDate noche) {
        return cantidadOcupantes > 0 ? cantidadOcupantes : 1;
    }

    public void transicionarA(EstadoReserva nuevoEstado) throws TransicionEstadoInvalidaException {
        if (nuevoEstado == null) {
            throw new TransicionEstadoInvalidaException("Estado no válido");
        }
        if (estado == EstadoReserva.PENDIENTE && nuevoEstado == EstadoReserva.CONFIRMADA) {
            estado = nuevoEstado;
            return;
        }
        if (estado == EstadoReserva.CONFIRMADA && nuevoEstado == EstadoReserva.EN_CURSO) {
            estado = nuevoEstado;
            return;
        }
        if (estado == EstadoReserva.EN_CURSO && nuevoEstado == EstadoReserva.FINALIZADA) {
            estado = nuevoEstado;
            return;
        }
        if (nuevoEstado == EstadoReserva.CANCELADA || nuevoEstado == EstadoReserva.NO_SHOW) {
            estado = nuevoEstado;
            return;
        }
        throw new TransicionEstadoInvalidaException("Transición inválida: " + estado + " -> " + nuevoEstado);
    }

    // TODO Integración Persona A: ListaDoblementeEnlazada<Ocupante> ocupantes y Pila<MovimientoFolio> folio

    @Override
    public int compareTo(Reserva otra) {
        return estancia.getFechaEntrada().compareTo(otra.estancia.getFechaEntrada());
    }

    @Override
    public String toString() {
        return idReserva == null ? "Reserva(" + titular + ")" : idReserva;
    }
}
