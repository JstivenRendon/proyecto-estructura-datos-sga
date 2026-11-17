package comparadores;

import java.util.Comparator;

import modelo.TareaPreparacion;

public class ComparadorPreparacion {
    private ComparadorPreparacion() {
    }

    public static Comparator<TareaPreparacion> crear() {
        return Comparator
                .comparing((TareaPreparacion tarea) -> !tarea.isEntradaHoy())
                .thenComparing(tarea -> tarea.getHoraLlegada(), Comparator.nullsLast(Comparator.naturalOrder()));
    }
}
