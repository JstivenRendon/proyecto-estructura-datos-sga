package estructuras;

import java.util.Iterator;

/**
 * Grafo ponderado no dirigido. Representa el mapa del edificio:
 * los vértices son lugares y las aristas son pasillos con peso.
 */
public class Grafo<T> implements Iterable<T> {

    public static class Arista<T> {
        private final T destino;
        private final double peso;

        public Arista(T destino, double peso) {
            this.destino = destino;
            this.peso = peso;
        }

        public T getDestino() {
            return destino;
        }

        public double getPeso() {
            return peso;
        }
    }

    private static class Vertice<T> {
        private final T dato;
        private final ListaEnlazada<Arista<T>> aristas = new ListaEnlazada<>();

        private Vertice(T dato) {
            this.dato = dato;
        }
    }

    private final ListaEnlazada<Vertice<T>> vertices = new ListaEnlazada<>();

    public boolean agregarVertice(T dato) {
        if (buscar(dato) != null) {
            return false;
        }
        vertices.agregarFinal(new Vertice<>(dato));
        return true;
    }

    public void agregarArista(T origen, T destino, double peso) {
        Vertice<T> a = buscar(origen);
        Vertice<T> b = buscar(destino);
        if (a == null || b == null) {
            throw new IllegalArgumentException("Los dos vértices deben existir");
        }
        if (peso < 0) {
            throw new IllegalArgumentException("El peso no puede ser negativo");
        }
        a.aristas.agregarFinal(new Arista<>(destino, peso));
        b.aristas.agregarFinal(new Arista<>(origen, peso));
    }

    public ListaEnlazada<Arista<T>> adyacentes(T dato) {
        Vertice<T> vertice = buscar(dato);
        if (vertice == null) {
            throw new IllegalArgumentException("El vértice no existe");
        }
        return vertice.aristas;
    }

    public boolean contiene(T dato) {
        return buscar(dato) != null;
    }

    public int cantidadVertices() {
        return vertices.tamano();
    }

    private Vertice<T> buscar(T dato) {
        for (Vertice<T> vertice : vertices) {
            if (vertice.dato.equals(dato)) {
                return vertice;
            }
        }
        return null;
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<Vertice<T>> interno = vertices.iterator();
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return interno.hasNext();
            }

            @Override
            public T next() {
                return interno.next().dato;
            }
        };
    }
}