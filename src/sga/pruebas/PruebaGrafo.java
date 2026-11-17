package pruebas;

import estructuras.ColaPrioridad;
import estructuras.Grafo;
import estructuras.ListaEnlazada;
import excepciones.EstructuraVaciaException;

import java.util.Comparator;

/**
 * Pruebas sencillas del Grafo (Dijkstra, BFS, DFS) y de la ColaPrioridad.
 * Se ejecuta con el main y cada prueba imprime OK o FALLO.
 */
public class PruebaGrafo {

    public static void main(String[] args) {
        probarGrafo();
        probarColaPrioridad();
    }

    private static void probarGrafo() {
        System.out.println("--- Grafo ---");

        Grafo<String> grafo = new Grafo<>();
        grafo.agregarVertice("R");
        grafo.agregarVertice("ESC-A");
        grafo.agregarVertice("A101");
        grafo.agregarVertice("ISLA"); // vértice sin pasillos

        // El pasillo directo R - A101 es más largo que pasar por la escalera
        grafo.agregarArista("R", "ESC-A", 12);
        grafo.agregarArista("R", "A101", 30);
        grafo.agregarArista("ESC-A", "A101", 6);

        comprobar("No se repite un vértice", !grafo.agregarVertice("R"));
        comprobar("El grafo tiene 4 vértices", grafo.cantidadVertices() == 4);

        // Dijkstra: 12 + 6 = 18 es menor que el pasillo directo de 30
        ListaEnlazada<String> ruta = grafo.dijkstra("R", "A101");
        comprobar("Dijkstra pasa por ESC-A (18 m) y no por el pasillo directo (30 m)",
                ruta.tamano() == 3 && ruta.obtener(1).equals("ESC-A"));

        comprobar("Dijkstra sin camino devuelve lista vacía", grafo.dijkstra("R", "ISLA").estaVacia());

        // BFS y DFS
        comprobar("BFS desde R visita 3 vértices", grafo.bfs("R").tamano() == 3);
        comprobar("DFS desde R visita 3 vértices", grafo.dfs("R").tamano() == 3);

        // Alcanzables
        comprobar("R llega a A101", grafo.alcanzable("R", "A101"));
        comprobar("R no llega a ISLA", !grafo.alcanzable("R", "ISLA"));
    }

    private static void probarColaPrioridad() {
        System.out.println("--- Cola de prioridad ---");

        Comparator<Integer> orden = Comparator.naturalOrder();
        ColaPrioridad<Integer> cola = new ColaPrioridad<>(orden);
        cola.encolar(5);
        cola.encolar(1);
        cola.encolar(3);

        comprobar("La cola tiene 3 elementos", cola.tamano() == 3);
        comprobar("Sale primero el menor (1)", cola.desencolar() == 1);
        comprobar("Sale después el 3", cola.desencolar() == 3);
        comprobar("Sale al final el 5", cola.desencolar() == 5);
        comprobar("La cola queda vacía", cola.estaVacia());

        try {
            cola.desencolar();
            comprobar("Desencolar una cola vacía debe lanzar excepción", false);
        } catch (EstructuraVaciaException e) {
            comprobar("Desencolar una cola vacía lanza EstructuraVaciaException", true);
        }
    }

    private static void comprobar(String descripcion, boolean resultado) {
        System.out.println((resultado ? "OK    " : "FALLO ") + descripcion);
    }
}