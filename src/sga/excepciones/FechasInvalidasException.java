package excepciones;

public class FechasInvalidasException extends Exception {
    private static final long serialVersionUID = 1L;

    public FechasInvalidasException(String mensaje) {
        super(mensaje);
    }
}
