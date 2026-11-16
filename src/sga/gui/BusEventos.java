package gui;

import estructuras.ListaEnlazada;

/**
 * Avisa a las pantallas cuando cambian los datos del sistema.
 */
public class BusEventos {
    private final ListaEnlazada<EscuchaCambios> oyentes = new ListaEnlazada<>();

    public void suscribir(EscuchaCambios oyente) {
        oyentes.agregarFinal(oyente);
    }

    public void cancelar(EscuchaCambios oyente) {
        oyentes.eliminar(oyente);
    }

    public void notificar() {
        for (EscuchaCambios oyente : oyentes) {
            oyente.alCambiar();
        }
    }
}