package excepciones;

public class ReservaSolapadaException extends Exception {
    private static final long serialVersionUID = 1L;

    public ReservaSolapadaException(String mensaje) {
        super(mensaje);
    }
}
