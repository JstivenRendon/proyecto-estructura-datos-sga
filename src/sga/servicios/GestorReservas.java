package servicios;

import java.time.LocalDateTime;

import estructuras.ArbolBinarioBusqueda;
import estructuras.ColaPrioridadContrato;
import estructuras.ColaPrioridadEnlazada;
import estructuras.ListaEnlazada;
import excepciones.CapacidadExcedidaException;
import excepciones.ReservaSolapadaException;
import excepciones.TransicionEstadoInvalidaException;
import modelo.Apartamento;
import modelo.CanalReserva;
import modelo.EstadoApartamento;
import modelo.EstadoOperativo;
import modelo.EstadoReserva;
import modelo.Estancia;
import modelo.Folio;
import modelo.Huesped;
import modelo.Reserva;

public class GestorReservas {
    private final ArbolBinarioBusqueda<Apartamento> catalogoApartamentos = new ArbolBinarioBusqueda<>();
    private final ArbolBinarioBusqueda<Huesped> catalogoHuespedes = new ArbolBinarioBusqueda<>();
    private final ListaEnlazada<Reserva> reservas = new ListaEnlazada<>();
    private final ColaPrioridadContrato<Reserva> reservasPendientes =
            new ColaPrioridadEnlazada<>(java.util.Comparator.comparing(Reserva::getFechaVencimiento));
    private Runnable callbackCheckout;

    public void registrarApartamento(Apartamento apartamento) {
        if (apartamento != null) {
            catalogoApartamentos.insertar(apartamento);
        }
    }

    public void registrarHuesped(Huesped huesped) {
        if (huesped != null) {
            catalogoHuespedes.insertar(huesped);
        }
    }

    public ArbolBinarioBusqueda<Apartamento> getCatalogoApartamentos() {
        return catalogoApartamentos;
    }

    public ArbolBinarioBusqueda<Huesped> getCatalogoHuespedes() {
        return catalogoHuespedes;
    }

    public ListaEnlazada<Reserva> getReservas() {
        return reservas;
    }

    public void setCallbackCheckout(Runnable callbackCheckout) {
        this.callbackCheckout = callbackCheckout;
    }

    public ListaEnlazada<Apartamento> buscarDisponibles(Estancia estancia, int cantidadPersonas,
            java.util.Comparator<Apartamento> criterioOrden) {
        ListaEnlazada<Apartamento> resultado = new ListaEnlazada<>();
        if (estancia == null) {
            return resultado;
        }
        for (Apartamento apartamento : catalogoApartamentos) {
            if (apartamento.getEstadoOperativo() == EstadoOperativo.FUERA_DE_SERVICIO) {
                continue;
            }
            if (apartamento.getCapacidadMaxima() < cantidadPersonas) {
                continue;
            }
            if (apartamento.getReservas().existeSolapamiento(estancia)) {
                continue;
            }
            insertarOrdenado(resultado, apartamento, criterioOrden);
        }
        return resultado;
    }

    public Reserva crearReserva(String documento, String codigoApartamento, Estancia estancia, CanalReserva canal)
            throws ReservaSolapadaException, CapacidadExcedidaException, TransicionEstadoInvalidaException {
        return crearReserva(documento, codigoApartamento, estancia, canal, (java.time.LocalDate[]) null);
    }

    public Reserva crearReserva(String documento, String codigoApartamento, Estancia estancia, CanalReserva canal,
            java.time.LocalDate[] fechasNacimientoOcupantes)
            throws ReservaSolapadaException, CapacidadExcedidaException, TransicionEstadoInvalidaException {
        int cantidadOcupantes = fechasNacimientoOcupantes == null ? 1 : fechasNacimientoOcupantes.length;
        return crearReservaInterna(documento, codigoApartamento, estancia, canal, cantidadOcupantes);
    }

    public Reserva crearReserva(String documento, String codigoApartamento, Estancia estancia, CanalReserva canal,
            LocalDateTime[] fechasNacimientoOcupantes)
            throws ReservaSolapadaException, CapacidadExcedidaException, TransicionEstadoInvalidaException {
        int cantidadOcupantes = fechasNacimientoOcupantes == null ? 1 : fechasNacimientoOcupantes.length;
        return crearReservaInterna(documento, codigoApartamento, estancia, canal, cantidadOcupantes);
    }

    private Reserva crearReservaInterna(String documento, String codigoApartamento, Estancia estancia, CanalReserva canal,
            int cantidadOcupantes)
            throws ReservaSolapadaException, CapacidadExcedidaException, TransicionEstadoInvalidaException {
        Huesped huesped = catalogoHuespedes.buscar(new Huesped(documento, "", null, "", ""));
        Apartamento apartamento = catalogoApartamentos.buscar(new Apartamento(codigoApartamento, "", "", 0, 0, 0,
                "", 0.0, EstadoOperativo.PREPARADO, null, null));
        if (huesped == null || apartamento == null) {
            throw new IllegalArgumentException("Huésped o apartamento no existente");
        }
        if (apartamento.getEstadoOperativo() == EstadoOperativo.FUERA_DE_SERVICIO) {
            throw new TransicionEstadoInvalidaException("El apartamento está fuera de servicio");
        }

        if (cantidadOcupantes > apartamento.getCapacidadMaxima()) {
            throw new CapacidadExcedidaException("La cantidad de ocupantes excede la capacidad del apartamento");
        }
        if (apartamento.getReservas().existeSolapamiento(estancia)) {
            throw new ReservaSolapadaException("El apartamento ya tiene reservas solapadas");
        }

        EstadoReserva estadoInicial = canal == CanalReserva.PORTAL ? EstadoReserva.PENDIENTE : EstadoReserva.CONFIRMADA;
        LocalDateTime ahora = LocalDateTime.now();
        String id = "RES-" + (reservas.tamano() + 1);
        Reserva reserva = new Reserva(id, huesped, apartamento, estancia, canal, estadoInicial, ahora, cantidadOcupantes);
        apartamento.agregarReserva(reserva);
        reservas.agregarFinal(reserva);
        if (estadoInicial == EstadoReserva.PENDIENTE) {
            reservasPendientes.encolar(reserva);
        }
        return reserva;
    }

    public void pagarAnticipo(String idReserva, double monto) throws TransicionEstadoInvalidaException {
        Reserva reserva = buscarReservaPorId(idReserva);
        if (reserva == null) {
            throw new IllegalArgumentException("Reserva no encontrada");
        }
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new TransicionEstadoInvalidaException("La reserva no está pendiente");
        }
        double tarifaPorNoche = reserva.getApartamento().getTarifaBasePorPersona() * reserva.getCantidadOcupantes();
        double valorTotal = tarifaPorNoche * contarNoches(reserva.getEstancia());
        if (monto < (valorTotal * 0.30)) {
            throw new TransicionEstadoInvalidaException("El anticipo no alcanza el 30%");
        }
        reserva.getFolio().registrarPago("Anticipo", monto);
        reserva.transicionarA(EstadoReserva.CONFIRMADA);
    }

    public void registrarCheckIn(String idReserva) throws TransicionEstadoInvalidaException {
        Reserva reserva = buscarReservaPorId(idReserva);
        if (reserva == null) {
            throw new IllegalArgumentException("Reserva no encontrada");
        }
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new TransicionEstadoInvalidaException("La reserva debe estar confirmada para check-in");
        }
        if (reserva.getApartamento().getEstadoOperativo() != EstadoOperativo.PREPARADO) {
            throw new TransicionEstadoInvalidaException("El apartamento no está preparado");
        }
        reserva.transicionarA(EstadoReserva.EN_CURSO);
        reserva.getApartamento().cambiarEstado(EstadoApartamento.OCUPADO);
    }

    public void registrarCheckOut(String idReserva) throws TransicionEstadoInvalidaException {
        Reserva reserva = buscarReservaPorId(idReserva);
        if (reserva == null) {
            throw new IllegalArgumentException("Reserva no encontrada");
        }
        if (reserva.getEstado() != EstadoReserva.EN_CURSO) {
            throw new TransicionEstadoInvalidaException("La reserva debe estar en curso para check-out");
        }
        reserva.transicionarA(EstadoReserva.FINALIZADA);
        reserva.getApartamento().cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION);
        if (callbackCheckout != null) {
            callbackCheckout.run();
        }
    }

    public double calcularReembolsoCancelacion(Reserva reserva, LocalDateTime fechaCancelacion) {
        if (reserva == null) {
            return 0.0;
        }
        double pagado = 0.0;
        for (modelo.MovimientoFolio movimiento : reserva.getFolio().getMovimientos()) {
            if (movimiento.getValor() < 0) {
                pagado += -movimiento.getValor();
            }
        }
        if (pagado <= 0) {
            return 0.0;
        }
        long dias = java.time.temporal.ChronoUnit.DAYS.between(fechaCancelacion.toLocalDate(), reserva.getEstancia().getFechaEntrada());
        if (dias >= 7) {
            return pagado;
        }
        if (dias >= 2) {
            return pagado * 0.50;
        }
        return 0.0;
    }

    public double cancelarReserva(String idReserva, LocalDateTime fechaCancelacion)
            throws TransicionEstadoInvalidaException {
        Reserva reserva = buscarReservaPorId(idReserva);
        if (reserva == null) {
            throw new IllegalArgumentException("Reserva no encontrada");
        }
        double reembolso = calcularReembolsoCancelacion(reserva, fechaCancelacion);
        reserva.transicionarA(EstadoReserva.CANCELADA);
        return reembolso;
    }

    public int procesarVencimientos(LocalDateTime fechaHoraActual) throws TransicionEstadoInvalidaException {
        int procesadas = 0;
        while (!reservasPendientes.estaVacia()) {
            Reserva reserva = reservasPendientes.verPrimero();
            if (reserva.getFechaVencimiento().isAfter(fechaHoraActual)) {
                break;
            }
            reservasPendientes.desencolar();
            if (reserva.getEstado() == EstadoReserva.PENDIENTE) {
                reserva.transicionarA(EstadoReserva.CANCELADA);
                procesadas++;
            }
        }
        return procesadas;
    }

    public Reserva buscarReservaPorId(String idReserva) {
        if (idReserva == null) {
            return null;
        }
        for (Reserva reserva : reservas) {
            if (idReserva.equals(reserva.getIdReserva())) {
                return reserva;
            }
        }
        return null;
    }

    private void insertarOrdenado(ListaEnlazada<Apartamento> lista, Apartamento apartamento,
            java.util.Comparator<Apartamento> criterioOrden) {
        if (criterioOrden == null) {
            lista.agregarFinal(apartamento);
            return;
        }
        ListaEnlazada<Apartamento> temporal = new ListaEnlazada<>();
        boolean insertado = false;
        for (Apartamento actual : lista) {
            if (!insertado && criterioOrden.compare(apartamento, actual) <= 0) {
                temporal.agregarFinal(apartamento);
                insertado = true;
            }
            temporal.agregarFinal(actual);
        }
        if (!insertado) {
            temporal.agregarFinal(apartamento);
        }
        lista.limpiar();
        for (Apartamento actual : temporal) {
            lista.agregarFinal(actual);
        }
    }

    private int contarNoches(Estancia estancia) {
        int contador = 0;
        for (java.time.LocalDate fecha : estancia) {
            contador++;
        }
        return contador;
    }
}
