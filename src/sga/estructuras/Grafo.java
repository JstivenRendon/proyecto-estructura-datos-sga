package estructuras;

import java.util.Comparator;
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

    /**
     * Datos temporales que Dijkstra necesita por cada vértice.
     */
    private static class Registro<T> {
        private final Vertice<T> vertice;
        private double distancia = Double.POSITIVE_INFINITY;
        private Registro<T> anterior;
        private boolean definitivo;

        private Registro(Vertice<T> vertice) {
            this.vertice = vertice;
        }
    }

    /**
     * Elemento de la cola de prioridad de Dijkstra: un vértice con la
     * distancia que tenía cuando se encoló.
     */
    private static class Entrada<T> {
        private final Registro<T> registro;
        private final double distancia;

        private Entrada(Registro<T> registro, double distancia) {
            this.registro = registro;
            this.distancia = distancia;
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
        return verticeObligatorio(dato).aristas;
    }

    public boolean contiene(T dato) {
        return buscar(dato) != null;
    }

    public int cantidadVertices() {
        return vertices.tamano();
    }

    /**
     * Recorrido en profundidad (recursivo). Devuelve los vértices
     * en el orden en que se visitan, desde el origen.
     */
    public ListaEnlazada<T> dfs(T origen) {
        verticeObligatorio(origen);
        ListaEnlazada<T> recorrido = new ListaEnlazada<>();
        Conjunto<T> visitados = new Conjunto<>();
        dfsRecursivo(origen, visitados, recorrido);
        return recorrido;
    }

    private void dfsRecursivo(T actual, Conjunto<T> visitados, ListaEnlazada<T> recorrido) {
        visitados.agregar(actual);
        recorrido.agregarFinal(actual);
        for (Arista<T> arista : buscar(actual).aristas) {
            T vecino = arista.getDestino();
            if (!visitados.contiene(vecino)) {
                dfsRecursivo(vecino, visitados, recorrido);
            }
        }
    }

    /**
     * Recorrido en amplitud con una cola. Visita primero los vecinos
     * más cercanos (por número de pasillos), nivel por nivel.
     */
    public ListaEnlazada<T> bfs(T origen) {
        verticeObligatorio(origen);
        ListaEnlazada<T> recorrido = new ListaEnlazada<>();
        Conjunto<T> visitados = new Conjunto<>();
        Cola<T> pendientes = new Cola<>();
        visitados.agregar(origen);
        pendientes.encolar(origen);
        while (!pendientes.estaVacia()) {
            T actual = pendientes.desencolar();
            recorrido.agregarFinal(actual);
            for (Arista<T> arista : buscar(actual).aristas) {
                T vecino = arista.getDestino();
                if (visitados.agregar(vecino)) {
                    pendientes.encolar(vecino);
                }
            }
        }
        return recorrido;
    }

    /**
     * Indica si desde el origen se puede llegar al destino por los pasillos.
     */
    public boolean alcanzable(T origen, T destino) {
        if (!contiene(origen) || !contiene(destino)) {
            return false;
        }
        for (T vertice : bfs(origen)) {
            if (vertice.equals(destino)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ruta más corta (por suma de pesos) entre dos vértices, usando Dijkstra
     * con una cola de prioridad. Devuelve la lista de vértices desde el origen
     * hasta el destino, o una lista vacía si no hay camino.
     */
    public ListaEnlazada<T> dijkstra(T origen, T destino) {
        verticeObligatorio(origen);
        verticeObligatorio(destino);

        ListaEnlazada<Registro<T>> registros = new ListaEnlazada<>();
        for (Vertice<T> vertice : vertices) {
            registros.agregarFinal(new Registro<>(vertice));
        }

        Registro<T> inicio = registroDe(registros, origen);
        inicio.distancia = 0;
        ColaPrioridad<Entrada<T>> pendientes =
                new ColaPrioridad<>(Comparator.comparingDouble((Entrada<T> e) -> e.distancia));
        pendientes.encolar(new Entrada<>(inicio, 0));

        while (!pendientes.estaVacia()) {
            Registro<T> actual = pendientes.desencolar().registro;
            if (actual.definitivo) {
                continue;
            }
            actual.definitivo = true;
            if (actual.vertice.dato.equals(destino)) {
                break;
            }
            for (Arista<T> arista : actual.vertice.aristas) {
                Registro<T> vecino = registroDe(registros, arista.getDestino());
                double nuevaDistancia = actual.distancia + arista.getPeso();
                if (!vecino.definitivo && nuevaDistancia < vecino.distancia) {
                    vecino.distancia = nuevaDistancia;
                    vecino.anterior = actual;
                    pendientes.encolar(new Entrada<>(vecino, nuevaDistancia));
                }
            }
        }
        return construirRuta(registroDe(registros, destino));
    }

    private ListaEnlazada<T> construirRuta(Registro<T> fin) {
        ListaEnlazada<T> ruta = new ListaEnlazada<>();
        if (fin.distancia == Double.POSITIVE_INFINITY) {
            return ruta;
        }
        Registro<T> actual = fin;
        while (actual != null) {
            ruta.agregarInicio(actual.vertice.dato);
            actual = actual.anterior;
        }
        return ruta;
    }

    private Registro<T> registroDe(ListaEnlazada<Registro<T>> registros, T dato) {
        for (Registro<T> registro : registros) {
            if (registro.vertice.dato.equals(dato)) {
                return registro;
            }
        }
        return null;
    }

    private Vertice<T> buscar(T dato) {
        for (Vertice<T> vertice : vertices) {
            if (vertice.dato.equals(dato)) {
                return vertice;
            }
        }
        return null;
    }

    private Vertice<T> verticeObligatorio(T dato) {
        Vertice<T> vertice = buscar(dato);
        if (vertice == null) {
            throw new IllegalArgumentException("El vértice no existe");
        }
        return vertice;
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