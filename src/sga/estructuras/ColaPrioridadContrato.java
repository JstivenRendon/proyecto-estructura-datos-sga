package estructuras;

public interface ColaPrioridadContrato<T> {
    void encolar(T dato);

    T desencolar();

    T verPrimero();

    boolean estaVacia();
}
