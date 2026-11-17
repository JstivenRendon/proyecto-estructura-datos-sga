package modelo;

import java.time.LocalTime;

public class TareaPreparacion {
    private final Apartamento apartamento;
    private final boolean entradaHoy;
    private final LocalTime horaLlegada;

    public TareaPreparacion(Apartamento apartamento, boolean entradaHoy, LocalTime horaLlegada) {
        this.apartamento = apartamento;
        this.entradaHoy = entradaHoy;
        this.horaLlegada = horaLlegada;
    }

    public Apartamento getApartamento() {
        return apartamento;
    }

    public boolean isEntradaHoy() {
        return entradaHoy;
    }

    public LocalTime getHoraLlegada() {
        return horaLlegada;
    }

    @Override
    public String toString() {
        return apartamento.getCodigo() + (entradaHoy ? " | entrada hoy" : " | preparación");
    }
}
