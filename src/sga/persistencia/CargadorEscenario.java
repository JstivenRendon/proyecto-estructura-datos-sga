package persistencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import modelo.Apartamento;
import modelo.CanalReserva;
import modelo.EstadoOperativo;
import modelo.EstadoReserva;
import modelo.Estancia;
import modelo.Huesped;
import modelo.NodoEdificio;
import modelo.Recepcionista;
import modelo.Reserva;
import modelo.Temporada;
import modelo.TipoTemporada;
import servicios.SistemaAlojamiento;

public final class CargadorEscenario {
    private CargadorEscenario() {
    }

    public static SistemaAlojamiento cargar() {
        SistemaAlojamiento sistema = new SistemaAlojamiento("Aparta-Suites UQ");

        Apartamento a101 = new Apartamento("A101", "Apto 101", "A", 1, 2, 4, "Vista jardin", 120000,
                EstadoOperativo.PREPARADO, null, LocalTime.of(15, 0));
        Apartamento a102 = new Apartamento("A102", "Apto 102", "A", 1, 2, 3, "Balcon", 110000,
                EstadoOperativo.PREPARADO, null, LocalTime.of(14, 30));
        Apartamento b201 = new Apartamento("B201", "Apto 201", "B", 2, 3, 5, "Suite", 145000,
                EstadoOperativo.PREPARADO, null, null);

        sistema.registrarApartamento(a101);
        sistema.registrarApartamento(a102);
        sistema.registrarApartamento(b201);

        Huesped h1 = new Huesped("1001", "Ana Gómez", LocalDate.of(1988, 5, 14), "3100000001", "ana@example.com");
        Huesped h2 = new Huesped("1003", "Luis Pérez", LocalDate.of(1994, 7, 8), "3100000002", "luis@example.com");
        sistema.registrarHuesped(h1);
        sistema.registrarHuesped(h2);

        sistema.registrarRecepcionista(new Recepcionista("R1", "Sofía"));
        sistema.registrarRecepcionista(new Recepcionista("R2", "Mateo"));

        sistema.registrarTemporada(new Temporada(TipoTemporada.BAJA, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31)));
        sistema.registrarTemporada(new Temporada(TipoTemporada.MEDIA, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 6, 30)));
        sistema.registrarTemporada(new Temporada(TipoTemporada.ALTA, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 31)));

        sistema.registrarNodoMapa(new NodoEdificio("R", NodoEdificio.Tipo.RECEPCION, null));
        sistema.registrarNodoMapa(new NodoEdificio("A101", NodoEdificio.Tipo.APARTAMENTO, a101));
        sistema.registrarNodoMapa(new NodoEdificio("A102", NodoEdificio.Tipo.APARTAMENTO, a102));
        sistema.registrarNodoMapa(new NodoEdificio("B201", NodoEdificio.Tipo.APARTAMENTO, b201));
        sistema.registrarPasillo("R", "A101", 30);
        sistema.registrarPasillo("R", "A102", 35);
        sistema.registrarPasillo("R", "B201", 55);

        try {
            Estancia estancia = new Estancia(LocalDate.now().plusDays(2), LocalDate.now().plusDays(5));
            Reserva reserva = new Reserva("RES-100", h1, a101, estancia, CanalReserva.DIRECTA,
                    EstadoReserva.PENDIENTE, LocalDateTime.now(), 2);
            a101.agregarReserva(reserva);
            sistema.getReservas().agregarFinal(reserva);
        } catch (Exception ignored) {
        }

        return sistema;
    }
}
