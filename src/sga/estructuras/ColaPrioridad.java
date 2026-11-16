package estructuras;

import excepciones.EstructuraVaciaException;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Cola de prioridad genérica implementada como heap binario (mínimo).
 * Sale primero el elemento que el comparador considera "menor".
 * Se usa para la cola de preparación y para las reservas por vencer.
 */
public class ColaPrioridad<T> implements Iterable<T> {
    private static final int CAPACIDAD_INICIAL = 16;

    private final Comparator<T> comparador;
    private T[] heap;
    private int tamano;

    @SuppressWarnings("unchecked")
    public ColaPrioridad(Comparator<T> comparador) {
        this.comparador = comparador;
        this.heap = (T[]) new Object[CAPACIDAD_INICIAL];
    }

    public void encolar(T dato) {
        if (tamano == heap.length) {
            crecer();
        }
        heap[tamano] = dato;
        tamano++;
        subir(tamano - 1);
    }

    public T desencolar() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("No se puede desencolar: la cola de prioridad está vacía");
        }
        T primero = heap[0];
        tamano--;
        heap[0] = heap[tamano];
        heap[tamano] = null;
        if (tamano > 0) {
            bajar(0);
        }
        return primero;
    }

    public T frente() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La cola de prioridad está vacía");
        }
        return heap[0];
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    public int tamano() {
        return tamano;
    }

    private void subir(int indice) {
        while (indice > 0) {
            int padre = (indice - 1) / 2;
            if (comparador.compare(heap[indice], heap[padre]) >= 0) {
                break;
            }
            intercambiar(indice, padre);
            indice = padre;
        }
    }

    private void bajar(int indice) {
        while (true) {
            int izquierdo = 2 * indice + 1;
            int derecho = izquierdo + 1;
            int menor = indice;
            if (izquierdo < tamano && comparador.compare(heap[izquierdo], heap[menor]) < 0) {
                menor = izquierdo;
            }
            if (derecho < tamano && comparador.compare(heap[derecho], heap[menor]) < 0) {
                menor = derecho;
            }
            if (menor == indice) {
                return;
            }
            intercambiar(indice, menor);
            indice = menor;
        }
    }

    private void intercambiar(int a, int b) {
        T temporal = heap[a];
        heap[a] = heap[b];
        heap[b] = temporal;
    }

    @SuppressWarnings("unchecked")
    private void crecer() {
        T[] nuevo = (T[]) new Object[heap.length * 2];
        for (int i = 0; i < tamano; i++) {
            nuevo[i] = heap[i];
        }
        heap = nuevo;
    }

    /**
     * Recorre los elementos en orden de prioridad, sin modificar la cola.
     */
    @Override
    public Iterator<T> iterator() {
        ColaPrioridad<T> copia = new ColaPrioridad<>(comparador);
        for (int i = 0; i < tamano; i++) {
            copia.encolar(heap[i]);
        }
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return !copia.estaVacia();
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return copia.desencolar();
            }
        };
    }
}