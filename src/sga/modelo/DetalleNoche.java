package modelo;

import java.time.LocalDate;

public class DetalleNoche {
    private final LocalDate fechaNoche;
    private final Temporada temporada;
    private final double tarifaPorPersona;
    private final int personasQuePagan;
    private final double subtotalNoche;

    public DetalleNoche(LocalDate fechaNoche, Temporada temporada,
            double tarifaPorPersona, int personasQuePagan, double subtotalNoche) {
        this.fechaNoche = fechaNoche;
        this.temporada = temporada;
        this.tarifaPorPersona = tarifaPorPersona;
        this.personasQuePagan = personasQuePagan;
        this.subtotalNoche = subtotalNoche;
    }

    public LocalDate getFechaNoche() {
        return fechaNoche;
    }

    public Temporada getTemporada() {
        return temporada;
    }

    public double getTarifaPorPersona() {
        return tarifaPorPersona;
    }

    public int getPersonasQuePagan() {
        return personasQuePagan;
    }

    public double getSubtotalNoche() {
        return subtotalNoche;
    }
}
