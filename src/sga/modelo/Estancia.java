package modelo;

import java.time.LocalDate;
import java.util.Iterator;

import excepciones.FechasInvalidasException;

public class Estancia implements Iterable<LocalDate> {
    private final LocalDate fechaEntrada;
    private final LocalDate fechaSalida;

    public Estancia(LocalDate fechaEntrada, LocalDate fechaSalida)
            throws FechasInvalidasException {
        if (fechaEntrada == null || fechaSalida == null
                || !fechaSalida.isAfter(fechaEntrada)) {
            throw new FechasInvalidasException(
                    "La entrada y salida deben existir y la salida ser posterior a la entrada");
        }
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
    }

    public LocalDate getFechaEntrada() {
        return fechaEntrada;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    @Override
    public Iterator<LocalDate> iterator() {
        return new Iterator<LocalDate>() {
            private LocalDate nocheActual = fechaEntrada;

            @Override
            public boolean hasNext() {
                return nocheActual.isBefore(fechaSalida);
            }

            @Override
            public LocalDate next() {
                if (!hasNext()) {
                    throw new IllegalStateException("No hay más noches en la estancia");
                }
                LocalDate noche = nocheActual;
                nocheActual = nocheActual.plusDays(1);
                return noche;
            }
        };
    }

    @Override
    public String toString() {
        return fechaEntrada + " a " + fechaSalida;
    }
}