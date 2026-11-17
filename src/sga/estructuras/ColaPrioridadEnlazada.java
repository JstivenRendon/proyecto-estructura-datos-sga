package estructuras;

import java.util.Comparator;

public class ColaPrioridadEnlazada<T> implements ColaPrioridadContrato<T> {
    private static class Nodo<T> {
        private final T dato;
        private Nodo<T> siguiente;

        private Nodo(T dato) {
            this.dato = dato;
        }
    }

    private final Comparator<T> comparador;
    private Nodo<T> frente;

    public ColaPrioridadEnlazada(Comparator<T> comparador) {
        this.comparador = comparador;
    }

    @Override
    public void encolar(T dato) {
        if (dato == null) {
            return;
        }
        Nodo<T> nuevo = new Nodo<>(dato);
        if (frente == null) {
            frente = nuevo;
            return;
        }
        if (comparador.compare(dato, frente.dato) <= 0) {
            nuevo.siguiente = frente;
            frente = nuevo;
            return;
        }
        Nodo<T> actual = frente;
        while (actual.siguiente != null && comparador.compare(actual.siguiente.dato, dato) <= 0) {
            actual = actual.siguiente;
        }
        nuevo.siguiente = actual.siguiente;
        actual.siguiente = nuevo;
    }

    @Override
    public T desencolar() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La cola de prioridad está vacía");
        }
        T dato = frente.dato;
        frente = frente.siguiente;
        return dato;
    }

    @Override
    public T verPrimero() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La cola de prioridad está vacía");
        }
        return frente.dato;
    }

    @Override
    public boolean estaVacia() {
        return frente == null;
    }
}
