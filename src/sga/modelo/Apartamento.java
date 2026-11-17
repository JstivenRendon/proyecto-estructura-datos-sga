package modelo;

import java.time.LocalDate;
import java.time.LocalTime;

import estructuras.ArbolBinarioBusqueda;
import excepciones.TransicionEstadoInvalidaException;

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
                       String motivoFueraDeServicio) {
        this(codigo, nombre, bloque, piso, numeroDormitorios, capacidadMaxima,
                caracteristicas, tarifaBasePorPersona, estadoOperativo,
                motivoFueraDeServicio, null);
    }

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

    public EstadoApartamento getEstado() {
        switch (estadoOperativo) {
            case PREPARADO:
                return EstadoApartamento.PREPARADO;
            case OCUPADO:
                return EstadoApartamento.OCUPADO;
            case PENDIENTE_PREPARACION:
                return EstadoApartamento.PENDIENTE_PREPARACION;
            case EN_PREPARACION:
                return EstadoApartamento.EN_PREPARACION;
            case FUERA_DE_SERVICIO:
                return EstadoApartamento.FUERA_DE_SERVICIO;
            default:
                return EstadoApartamento.PREPARADO;
        }
    }

    /**
     * Cambia el estado del apartamento solo si la transición es válida.
     * Para pasar a Fuera de servicio se usa ponerFueraDeServicio(motivo).
     */
    public void cambiarEstado(EstadoApartamento nuevoEstado) throws TransicionEstadoInvalidaException {
        if (nuevoEstado == EstadoApartamento.FUERA_DE_SERVICIO) {
            throw new TransicionEstadoInvalidaException(
                    "Para pasar a Fuera de servicio use ponerFueraDeServicio(motivo)");
        }
        validarTransicion(nuevoEstado);

        switch (nuevoEstado) {
            case PREPARADO:
                this.estadoOperativo = EstadoOperativo.PREPARADO;
                break;
            case OCUPADO:
                this.estadoOperativo = EstadoOperativo.OCUPADO;
                break;
            case PENDIENTE_PREPARACION:
                this.estadoOperativo = EstadoOperativo.PENDIENTE_PREPARACION;
                break;
            case EN_PREPARACION:
                this.estadoOperativo = EstadoOperativo.EN_PREPARACION;
                break;
            default:
                break;
        }
        // Si estaba fuera de servicio, ya no hay motivo
        this.motivoFueraDeServicio = null;
    }

    /**
     * Deja el apartamento fuera de servicio. El motivo es obligatorio.
     */
    public void ponerFueraDeServicio(String motivo) throws TransicionEstadoInvalidaException {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new TransicionEstadoInvalidaException(
                    "Debe indicar el motivo para dejar el apartamento fuera de servicio");
        }
        validarTransicion(EstadoApartamento.FUERA_DE_SERVICIO);
        this.estadoOperativo = EstadoOperativo.FUERA_DE_SERVICIO;
        this.motivoFueraDeServicio = motivo.trim();
    }

    /**
     * Revisa si se puede pasar del estado actual al nuevo estado.
     * Ciclo normal: Preparado -> Ocupado -> Pendiente -> En preparación -> Preparado.
     */
    private void validarTransicion(EstadoApartamento nuevo) throws TransicionEstadoInvalidaException {
        EstadoApartamento actual = getEstado();
        boolean permitido = false;

        switch (actual) {
            case PREPARADO:
                permitido = (nuevo == EstadoApartamento.OCUPADO
                        || nuevo == EstadoApartamento.FUERA_DE_SERVICIO);
                break;
            case OCUPADO:
                permitido = (nuevo == EstadoApartamento.PENDIENTE_PREPARACION);
                break;
            case PENDIENTE_PREPARACION:
                permitido = (nuevo == EstadoApartamento.EN_PREPARACION
                        || nuevo == EstadoApartamento.FUERA_DE_SERVICIO);
                break;
            case EN_PREPARACION:
                permitido = (nuevo == EstadoApartamento.PREPARADO);
                break;
            case FUERA_DE_SERVICIO:
                permitido = (nuevo == EstadoApartamento.PENDIENTE_PREPARACION);
                break;
            default:
                break;
        }

        if (!permitido) {
            throw new TransicionEstadoInvalidaException(
                    "No se puede pasar de " + actual + " a " + nuevo);
        }
    }

    public void setTarifaBasePorPersona(double tarifaBasePorPersona) {
        this.tarifaBasePorPersona = tarifaBasePorPersona;
    }

    public void agregarReserva(Reserva reserva) {
        if (reserva != null) {
            reservas.insertar(reserva);
        }
    }

    public boolean existeSolapamiento(Estancia nuevaEstancia) {
        return reservas.existeSolapamiento(nuevaEstancia);
    }

    public boolean haySolapamiento(Estancia estancia) {
        return existeSolapamiento(estancia);
    }

    public boolean disponibleEn(Estancia estancia) {
        return !haySolapamiento(estancia);
    }

    public double tarifa(TipoTemporada temporada) {
        if (temporada == null) {
            return tarifaBasePorPersona;
        }
        switch (temporada) {
            case ALTA:
                return tarifaBasePorPersona * 1.30;
            case MEDIA:
                return tarifaBasePorPersona * 1.15;
            default:
                return tarifaBasePorPersona;
        }
    }

    public Reserva reservaConEntrada(LocalDate fecha) {
        for (Reserva reserva : reservas) {
            if (reserva.getEstancia().getFechaEntrada().equals(fecha)
                    && reserva.getEstado() != EstadoReserva.CANCELADA
                    && reserva.getEstado() != EstadoReserva.NO_SHOW
                    && reserva.getEstado() != EstadoReserva.FINALIZADA) {
                return reserva;
            }
        }
        return null;
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