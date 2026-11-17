package comparadores;

import java.util.Comparator;

import modelo.TareaPreparacion;

/**
 * Orden de la cola de preparación, combinando comparadores con thenComparing:
 * 1) Prioridad 1 (entrada hoy) antes que Prioridad 2.
 * 2) Entre las de entrada hoy, la que llega más temprano.
 * 3) Si aún hay empate, la que entró primero a la cola (justicia por orden de llegada).
 */
public class ComparadorPreparacion {
    private ComparadorPreparacion() {
    }

    public static Comparator<TareaPreparacion> crear() {
        return Comparator
                .comparing((TareaPreparacion tarea) -> !tarea.isEntradaHoy())
                .thenComparing(tarea -> tarea.getHoraLlegada(), Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(TareaPreparacion::getHoraEncolada);
    }
}