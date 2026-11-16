package estructuras;

import java.util.Iterator;

public class ArbolBinarioBusqueda<T extends Comparable<T>> implements Iterable<T> {
    @FunctionalInterface
    public interface Accion<T> {
        void ejecutar(T dato);
    }

    private static final class NodoBST<T> {
        private T dato;
        private NodoBST<T> izquierdo;
        private NodoBST<T> derecho;

        private NodoBST(T dato) {
            this.dato = dato;
        }
    }

    private static final class NodoPila<T> {
        private final NodoBST<T> nodo;
        private final NodoPila<T> siguiente;

        private NodoPila(NodoBST<T> nodo, NodoPila<T> siguiente) {
            this.nodo = nodo;
            this.siguiente = siguiente;
        }
    }

    private NodoBST<T> raiz;

    public void insertar(T dato) {
        if (dato == null) {
            throw new IllegalArgumentException("El dato no puede ser null");
        }
        raiz = insertarRecursivo(raiz, dato);
    }

    private NodoBST<T> insertarRecursivo(NodoBST<T> actual, T dato) {
        if (actual == null) {
            return new NodoBST<>(dato);
        }
        int comparacion = dato.compareTo(actual.dato);
        if (comparacion < 0) {
            actual.izquierdo = insertarRecursivo(actual.izquierdo, dato);
        } else if (comparacion > 0) {
            actual.derecho = insertarRecursivo(actual.derecho, dato);
        }
        return actual;
    }

    public T buscar(T dato) {
        if (dato == null) {
            return null;
        }
        NodoBST<T> encontrado = buscarRecursivo(raiz, dato);
        return encontrado == null ? null : encontrado.dato;
    }

    private NodoBST<T> buscarRecursivo(NodoBST<T> actual, T dato) {
        if (actual == null) {
            return null;
        }
        int comparacion = dato.compareTo(actual.dato);
        if (comparacion == 0) {
            return actual;
        }
        return comparacion < 0
                ? buscarRecursivo(actual.izquierdo, dato)
                : buscarRecursivo(actual.derecho, dato);
    }

    public boolean eliminar(T dato) {
        if (dato == null || buscar(dato) == null) {
            return false;
        }
        raiz = eliminarRecursivo(raiz, dato);
        return true;
    }

    private NodoBST<T> eliminarRecursivo(NodoBST<T> actual, T dato) {
        int comparacion = dato.compareTo(actual.dato);
        if (comparacion < 0) {
            actual.izquierdo = eliminarRecursivo(actual.izquierdo, dato);
        } else if (comparacion > 0) {
            actual.derecho = eliminarRecursivo(actual.derecho, dato);
        } else if (actual.izquierdo == null) {
            return actual.derecho;
        } else if (actual.derecho == null) {
            return actual.izquierdo;
        } else {
            NodoBST<T> sucesor = minimo(actual.derecho);
            actual.dato = sucesor.dato;
            actual.derecho = eliminarRecursivo(actual.derecho, sucesor.dato);
        }
        return actual;
    }

    private NodoBST<T> minimo(NodoBST<T> actual) {
        if (actual.izquierdo == null) {
            return actual;
        }
        return minimo(actual.izquierdo);
    }

    public int calcularAltura() {
        return calcularAlturaRecursiva(raiz);
    }

    private int calcularAlturaRecursiva(NodoBST<T> actual) {
        if (actual == null) {
            return -1;
        }
        int alturaIzquierda = calcularAlturaRecursiva(actual.izquierdo);
        int alturaDerecha = calcularAlturaRecursiva(actual.derecho);
        return 1 + (alturaIzquierda > alturaDerecha ? alturaIzquierda : alturaDerecha);
    }

    public void recorridoInorden() {
        recorridoInorden(System.out::println);
    }

    public void recorridoInorden(Accion<T> accion) {
        validarAccion(accion);
        recorridoInordenRecursivo(raiz, accion);
    }

    private void recorridoInordenRecursivo(NodoBST<T> actual, Accion<T> accion) {
        if (actual != null) {
            recorridoInordenRecursivo(actual.izquierdo, accion);
            accion.ejecutar(actual.dato);
            recorridoInordenRecursivo(actual.derecho, accion);
        }
    }

    public void recorridoPreorden() {
        recorridoPreorden(System.out::println);
    }

    public void recorridoPreorden(Accion<T> accion) {
        validarAccion(accion);
        recorridoPreordenRecursivo(raiz, accion);
    }

    private void recorridoPreordenRecursivo(NodoBST<T> actual, Accion<T> accion) {
        if (actual != null) {
            accion.ejecutar(actual.dato);
            recorridoPreordenRecursivo(actual.izquierdo, accion);
            recorridoPreordenRecursivo(actual.derecho, accion);
        }
    }

    public void recorridoPostorden() {
        recorridoPostorden(System.out::println);
    }

    public void recorridoPostorden(Accion<T> accion) {
        validarAccion(accion);
        recorridoPostordenRecursivo(raiz, accion);
    }

    private void recorridoPostordenRecursivo(NodoBST<T> actual, Accion<T> accion) {
        if (actual != null) {
            recorridoPostordenRecursivo(actual.izquierdo, accion);
            recorridoPostordenRecursivo(actual.derecho, accion);
            accion.ejecutar(actual.dato);
        }
    }

    private void validarAccion(Accion<T> accion) {
        if (accion == null) {
            throw new IllegalArgumentException("La acción no puede ser null");
        }
    }

    public int contarElementos() {
        return contarElementosRecursivo(raiz);
    }

    private int contarElementosRecursivo(NodoBST<T> actual) {
        if (actual == null) {
            return 0;
        }
        return 1 + contarElementosRecursivo(actual.izquierdo)
                + contarElementosRecursivo(actual.derecho);
    }

    public boolean estaVacio() {
        return raiz == null;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private NodoPila<T> cima;

            {
                apilarIzquierdos(raiz);
            }

            private void apilarIzquierdos(NodoBST<T> nodo) {
                NodoBST<T> actual = nodo;
                while (actual != null) {
                    cima = new NodoPila<>(actual, cima);
                    actual = actual.izquierdo;
                }
            }

            @Override
            public boolean hasNext() {
                return cima != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new IllegalStateException("No hay más elementos en el árbol");
                }
                NodoBST<T> siguiente = cima.nodo;
                cima = cima.siguiente;
                apilarIzquierdos(siguiente.derecho);
                return siguiente.dato;
            }
        };
    }
}
