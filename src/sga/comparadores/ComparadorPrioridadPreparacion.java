package comparadores;

import java.util.Comparator;

import modelo.Apartamento;

public class ComparadorPrioridadPreparacion implements Comparator<Apartamento> {
    private final Comparator<Apartamento> comparador = Comparator
            .comparing((Apartamento apartamento) ->
                    apartamento.getHoraLlegadaProgramadaHoy() == null)
            .thenComparing(Apartamento::getHoraLlegadaProgramadaHoy,
                    Comparator.nullsLast(Comparator.naturalOrder()));

    @Override
    public int compare(Apartamento primero, Apartamento segundo) {
        return comparador.compare(primero, segundo);
    }
}
