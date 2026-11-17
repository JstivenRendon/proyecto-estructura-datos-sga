package modelo;

import java.time.LocalDate;

public class NocheLiquidada {
    private final LocalDate noche;
    private final TipoTemporada temporada;
    private final int personasQuePagan;
    private final double tarifa;
    private final double subtotal;

    public NocheLiquidada(LocalDate noche, TipoTemporada temporada, int personasQuePagan, double tarifa) {
        this.noche = noche;
        this.temporada = temporada;
        this.personasQuePagan = personasQuePagan;
        this.tarifa = tarifa;
        this.subtotal = personasQuePagan * tarifa;
    }

    public LocalDate getNoche() {
        return noche;
    }

    public TipoTemporada getTemporada() {
        return temporada;
    }

    public int getPersonasQuePagan() {
        return personasQuePagan;
    }

    public double getTarifa() {
        return tarifa;
    }

    public double getSubtotal() {
        return subtotal;
    }

    @Override
    public String toString() {
        return noche + " | " + temporada + " | " + personasQuePagan + " pers. | subtotal=" + subtotal;
    }
}
