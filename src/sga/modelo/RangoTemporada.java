package modelo;

import java.time.LocalDate;

public class RangoTemporada {
    private final Temporada temporada;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final double multiplicadorTarifa;

    public RangoTemporada(Temporada temporada, LocalDate fechaInicio, LocalDate fechaFin, double multiplicadorTarifa) {
        this.temporada = temporada;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.multiplicadorTarifa = multiplicadorTarifa;
    }

    public Temporada getTemporada() {
        return temporada;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public double getMultiplicadorTarifa() {
        return multiplicadorTarifa;
    }

    public boolean cubre(LocalDate fecha) {
        return fecha != null && !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
    }
}
