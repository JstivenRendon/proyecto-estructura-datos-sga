package modelo;

import java.time.LocalDate;

import excepciones.FechasInvalidasException;

public class SolicitudExterna {
    private final String plataforma;
    private final String documentoTitular;
    private final String codigoApartamento;
    private final Estancia estancia;
    private final int personas;

    public SolicitudExterna(String plataforma, String documentoTitular, String codigoApartamento,
                            Estancia estancia, int personas) {
        this.plataforma = plataforma;
        this.documentoTitular = documentoTitular;
        this.codigoApartamento = codigoApartamento;
        this.estancia = estancia;
        this.personas = personas;
    }

    public SolicitudExterna(String plataforma, String documentoTitular, String codigoApartamento,
                            LocalDate fechaEntrada, LocalDate fechaSalida, int personas) {
        try {
            this.plataforma = plataforma;
            this.documentoTitular = documentoTitular;
            this.codigoApartamento = codigoApartamento;
            this.estancia = new Estancia(fechaEntrada, fechaSalida);
            this.personas = personas;
        } catch (FechasInvalidasException e) {
            throw new IllegalArgumentException("Fechas inválidas para la solicitud externa", e);
        }
    }

    public String getPlataforma() {
        return plataforma;
    }

    public String getDocumentoTitular() {
        return documentoTitular;
    }

    public String getCodigoApartamento() {
        return codigoApartamento;
    }

    public Estancia getEstancia() {
        return estancia;
    }

    public int getPersonas() {
        return personas;
    }

    @Override
    public String toString() {
        return plataforma + " → " + codigoApartamento + " " + estancia + " (" + personas + " pers.)";
    }
}
