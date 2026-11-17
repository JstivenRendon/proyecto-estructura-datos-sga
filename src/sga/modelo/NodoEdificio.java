package modelo;

public class NodoEdificio {
    public enum Tipo {
        RECEPCION,
        APARTAMENTO,
        SERVICIO
    }

    private final String id;
    private final Tipo tipo;
    private final Apartamento apartamento;

    public NodoEdificio(String id, Tipo tipo, Apartamento apartamento) {
        this.id = id;
        this.tipo = tipo;
        this.apartamento = apartamento;
    }

    public String getId() {
        return id;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public Apartamento getApartamento() {
        return apartamento;
    }

    @Override
    public String toString() {
        return id;
    }
}
