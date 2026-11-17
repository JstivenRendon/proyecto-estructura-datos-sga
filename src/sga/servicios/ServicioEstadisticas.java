package servicios;

import estructuras.ListaEnlazada;
import modelo.Apartamento;
import modelo.CanalReserva;
import modelo.EstadoReserva;
import modelo.MovimientoFolio;
import modelo.NocheLiquidada;
import modelo.Reserva;
import modelo.TipoTemporada;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Calcula las estadísticas del alojamiento recorriendo las estructuras del
 * sistema con sus iteradores (árbol de apartamentos, lista de reservas y
 * lista de conflictos).
 *
 * El periodo va desde la noche "desde" (incluida) hasta la fecha "hasta"
 * (no incluida), igual que una estancia: del 1 al 3 son dos noches.
 */
public class ServicioEstadisticas {
    private final SistemaAlojamiento sistema;
    private final LocalDate desde;
    private final LocalDate hasta;

    public ServicioEstadisticas(SistemaAlojamiento sistema, LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || !hasta.isAfter(desde)) {
            throw new IllegalArgumentException("El periodo debe tener al menos una noche");
        }
        this.sistema = sistema;
        this.desde = desde;
        this.hasta = hasta;
    }

    public long nochesDelPeriodo() {
        return ChronoUnit.DAYS.between(desde, hasta);
    }

    // ------------------------------------------------------- reservas por canal

    /**
     * Cuenta las reservas de un canal. Si el estado es null cuenta todas;
     * si no, solo las que están en ese estado (por ejemplo CANCELADA).
     */
    public int contarReservas(CanalReserva canal, EstadoReserva estado) {
        int total = 0;
        for (Reserva reserva : sistema.getReservas()) {
            if (reserva.getCanal() == canal && (estado == null || reserva.getEstado() == estado)) {
                total++;
            }
        }
        return total;
    }

    // ---------------------------------------------------------------- ocupación

    /** Porcentaje de noches ocupadas de un apartamento dentro del periodo. */
    public double porcentajeOcupacion(Apartamento apartamento) {
        long ocupadas = 0;
        for (Reserva reserva : apartamento.getReservas()) {
            if (ocupaNoches(reserva)) {
                ocupadas += nochesEnPeriodo(reserva);
            }
        }
        return ocupadas * 100.0 / nochesDelPeriodo();
    }

    /** Noches ocupadas, dentro del periodo, que caen en una temporada. */
    public int nochesOcupadasPorTemporada(TipoTemporada temporada) {
        int total = 0;
        for (Reserva reserva : sistema.getReservas()) {
            if (!ocupaNoches(reserva)) {
                continue;
            }
            for (LocalDate noche : reserva.getEstancia()) {
                if (dentroDelPeriodo(noche) && sistema.temporadaDe(noche) == temporada) {
                    total++;
                }
            }
        }
        return total;
    }

    /** Noches que había disponibles en el periodo para una temporada (apartamentos x días). */
    public int nochesDisponiblesPorTemporada(TipoTemporada temporada) {
        int apartamentos = sistema.getApartamentos().contarElementos();
        int total = 0;
        for (LocalDate dia = desde; dia.isBefore(hasta); dia = dia.plusDays(1)) {
            if (sistema.temporadaDe(dia) == temporada) {
                total += apartamentos;
            }
        }
        return total;
    }

    // ------------------------------------------------------------------ ingresos

    /**
     * Ingresos estimados: valor de las noches liquidadas, dentro del periodo,
     * de las reservas confirmadas, en curso o finalizadas.
     */
    public double ingresosEstimados() {
        double total = 0;
        for (Reserva reserva : sistema.getReservas()) {
            EstadoReserva estado = reserva.getEstado();
            boolean genera = estado == EstadoReserva.CONFIRMADA
                    || estado == EstadoReserva.EN_CURSO
                    || estado == EstadoReserva.FINALIZADA;
            if (!genera) {
                continue;
            }
            ListaEnlazada<NocheLiquidada> noches = sistema.liquidar(
                    reserva.getApartamento(), reserva.getEstancia(), reserva);
            for (NocheLiquidada noche : noches) {
                if (dentroDelPeriodo(noche.getNoche())) {
                    total += noche.getSubtotal();
                }
            }
        }
        return total;
    }

    /**
     * Dinero realmente recibido según los folios. Los pagos se guardan con
     * valor negativo; si un pago se deshizo, su movimiento contrario
     * (un cargo marcado como reverso) se resta.
     */
    public double pagosRecibidos() {
        double total = 0;
        for (Reserva reserva : sistema.getReservas()) {
            for (MovimientoFolio movimiento : reserva.getFolio().getMovimientos()) {
                if (movimiento.getTipo() == MovimientoFolio.Tipo.PAGO && !movimiento.isEsReverso()) {
                    total += -movimiento.getValor();
                } else if (movimiento.getTipo() == MovimientoFolio.Tipo.CARGO && movimiento.isEsReverso()) {
                    total -= movimiento.getValor();
                }
            }
        }
        return total;
    }

    // ------------------------------------------------------------------ reporte

    public String generarReporte() {
        StringBuilder sb = new StringBuilder();
        sb.append("Periodo: ").append(desde).append(" a ").append(hasta.minusDays(1))
                .append(" (").append(nochesDelPeriodo()).append(" noches)\n\n");

        sb.append("RESERVAS POR CANAL\n");
        sb.append(String.format("  %-10s %7s %12s %9s%n", "Canal", "Total", "Canceladas", "No-show"));
        for (CanalReserva canal : CanalReserva.values()) {
            sb.append(String.format("  %-10s %7d %12d %9d%n", canal,
                    contarReservas(canal, null),
                    contarReservas(canal, EstadoReserva.CANCELADA),
                    contarReservas(canal, EstadoReserva.NO_SHOW)));
        }

        sb.append("\nOCUPACION POR APARTAMENTO\n");
        for (Apartamento apartamento : sistema.getApartamentos()) {
            sb.append(String.format("  %-8s %6.1f %%%n", apartamento.getCodigo(), porcentajeOcupacion(apartamento)));
        }

        sb.append("\nOCUPACION POR TEMPORADA\n");
        for (TipoTemporada temporada : TipoTemporada.values()) {
            int ocupadas = nochesOcupadasPorTemporada(temporada);
            int disponibles = nochesDisponiblesPorTemporada(temporada);
            double porcentaje = disponibles == 0 ? 0 : ocupadas * 100.0 / disponibles;
            sb.append(String.format("  %-6s %4d de %4d noches (%.1f %%)%n", temporada, ocupadas, disponibles, porcentaje));
        }

        sb.append("\nINGRESOS DEL PERIODO\n");
        sb.append(String.format("  Estimados por noches liquidadas: $%,.0f%n", ingresosEstimados()));
        sb.append(String.format("  Pagos recibidos en folios:       $%,.0f%n", pagosRecibidos()));

        sb.append("\nCONFLICTOS DE CANAL (").append(sistema.getConflictosCanal().tamano()).append(")\n");
        if (sistema.getConflictosCanal().estaVacia()) {
            sb.append("  Ninguno\n");
        }
        for (String conflicto : sistema.getConflictosCanal()) {
            sb.append("  - ").append(conflicto).append("\n");
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------- apoyo

    /** Una reserva ocupa noches mientras no esté cancelada ni sea no-show. */
    private boolean ocupaNoches(Reserva reserva) {
        return reserva.getEstado() != EstadoReserva.CANCELADA
                && reserva.getEstado() != EstadoReserva.NO_SHOW;
    }

    private boolean dentroDelPeriodo(LocalDate noche) {
        return !noche.isBefore(desde) && noche.isBefore(hasta);
    }

    /** Cuántas noches de la reserva caen dentro del periodo. */
    private long nochesEnPeriodo(Reserva reserva) {
        LocalDate entrada = reserva.getEstancia().getFechaEntrada();
        LocalDate salida = reserva.getEstancia().getFechaSalida();
        LocalDate inicio = entrada.isAfter(desde) ? entrada : desde;
        LocalDate fin = salida.isBefore(hasta) ? salida : hasta;
        return Math.max(0, ChronoUnit.DAYS.between(inicio, fin));
    }
}