package servicios;

import java.time.LocalDate;
import java.time.Period;
import java.util.Iterator;

import estructuras.ListaEnlazada;
import excepciones.CapacidadExcedidaException;
import modelo.Apartamento;
import modelo.CotizacionResult;
import modelo.DetalleNoche;
import modelo.Estancia;
import modelo.Temporada;

public class CotizadorEstancia {
    private final GestorTemporadas gestorTemporadas;

    public CotizadorEstancia() {
        this(new GestorTemporadas());
    }

    public CotizadorEstancia(GestorTemporadas gestorTemporadas) {
        this.gestorTemporadas = gestorTemporadas == null ? new GestorTemporadas() : gestorTemporadas;
    }

    public CotizacionResult cotizar(Apartamento apto, Estancia estancia, LocalDate[] fechasNacimientoOcupantes)
            throws CapacidadExcedidaException {
        if (apto == null || estancia == null) {
            throw new IllegalArgumentException("Apartamento y estancia no pueden ser null");
        }
        if (fechasNacimientoOcupantes == null) {
            throw new IllegalArgumentException("Debe indicarse la lista de ocupantes");
        }
        if (fechasNacimientoOcupantes.length > apto.getCapacidadMaxima()) {
            throw new CapacidadExcedidaException("La capacidad excede la del apartamento");
        }

        int totalOcupantes = fechasNacimientoOcupantes.length;
        int ocupantesQuePagan = 0;
        for (LocalDate fechaNacimiento : fechasNacimientoOcupantes) {
            if (fechaNacimiento != null && Period.between(fechaNacimiento, estancia.getFechaEntrada()).getYears() >= 6) {
                ocupantesQuePagan++;
            }
        }

        ListaEnlazada<DetalleNoche> noches = new ListaEnlazada<>();
        Iterator<LocalDate> iterador = estancia.iterator();
        cotizarRecursivo(apto, iterador, noches, fechasNacimientoOcupantes, estancia.getFechaEntrada(), ocupantesQuePagan);

        DetalleNoche[] desglose = convertirAArray(noches);
        double valorTotal = 0;
        for (DetalleNoche detalle : desglose) {
            valorTotal += detalle.getSubtotalNoche();
        }
        double anticipoRequerido = valorTotal * 0.30;
        return new CotizacionResult(desglose, valorTotal, anticipoRequerido, totalOcupantes, ocupantesQuePagan);
    }

    private void cotizarRecursivo(Apartamento apto, Iterator<LocalDate> iterator,
            ListaEnlazada<DetalleNoche> noches, LocalDate[] fechasNacimientoOcupantes,
            LocalDate fechaEntrada, int ocupantesQuePagan) {
        if (!iterator.hasNext()) {
            return;
        }
        LocalDate fechaNoche = iterator.next();
        Temporada temporada = gestorTemporadas.obtenerTemporada(fechaNoche);
        double tarifaPorPersona = apto.getTarifaBasePorPersona() * gestorTemporadas.obtenerMultiplicadorTarifa(temporada);
        double subtotalNoche = tarifaPorPersona * ocupantesQuePagan;
        noches.agregarFinal(new DetalleNoche(fechaNoche, temporada, tarifaPorPersona, ocupantesQuePagan, subtotalNoche));
        cotizarRecursivo(apto, iterator, noches, fechasNacimientoOcupantes, fechaEntrada, ocupantesQuePagan);
    }

    private DetalleNoche[] convertirAArray(ListaEnlazada<DetalleNoche> noches) {
        DetalleNoche[] resultado = new DetalleNoche[noches.tamano()];
        int indice = 0;
        for (DetalleNoche detalle : noches) {
            resultado[indice] = detalle;
            indice++;
        }
        return resultado;
    }
}
