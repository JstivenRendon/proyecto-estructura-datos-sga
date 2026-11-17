package servicios;

import java.time.LocalDate;

import excepciones.ReservaSolapadaException;
import modelo.Apartamento;
import modelo.Estancia;
import modelo.Reserva;

public final class ValidadorDisponibilidad {
    private ValidadorDisponibilidad() {
    }

    public static boolean existeSolapamiento(Apartamento apartamento, Estancia nuevaEstancia) {
        if (apartamento == null || nuevaEstancia == null) {
            return false;
        }
        return apartamento.getReservas().existeSolapamiento(nuevaEstancia);
    }

    public static void validarDisponibilidad(Apartamento apartamento, Estancia nuevaEstancia)
            throws ReservaSolapadaException {
        if (existeSolapamiento(apartamento, nuevaEstancia)) {
            throw new ReservaSolapadaException("El apartamento presenta solapamiento de reservas");
        }
    }

    public static boolean haySolapamientoEntre(Reserva reserva, Estancia nuevaEstancia) {
        if (reserva == null || nuevaEstancia == null) {
            return false;
        }
        LocalDate entrada = reserva.getEstancia().getFechaEntrada();
        LocalDate salida = reserva.getEstancia().getFechaSalida();
        return nuevaEstancia.getFechaEntrada().isBefore(salida)
                && entrada.isBefore(nuevaEstancia.getFechaSalida());
    }
}
