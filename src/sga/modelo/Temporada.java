package modelo;

import java.time.LocalDate;

public class Temporada {
    private final TipoTemporada tipo;
    private final LocalDate inicio;
    private final LocalDate fin;

    public Temporada(TipoTemporada tipo, LocalDate inicio, LocalDate fin) {
        this.tipo = tipo;
        this.inicio = inicio;
        this.fin = fin;
    }

    public boolean cubre(LocalDate fecha) {
        return fecha != null && !fecha.isBefore(inicio) && !fecha.isAfter(fin);
    }

    public TipoTemporada getTipo() {
        return tipo;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public LocalDate getFin() {
        return fin;
    }
}
