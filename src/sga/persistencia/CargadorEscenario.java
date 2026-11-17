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
import modelo.TareaPreparacion;
import modelo.Temporada;
import modelo.TipoTemporada;
import servicios.SistemaAlojamiento;

/**
 * Carga el escenario inicial de prueba: edificio con dos bloques, apartamentos
 * en distintos estados, huéspedes, reservas, temporadas, el mapa de pasillos
 * y la cola de aseo con tareas pendientes.
 */
public final class CargadorEscenario {
    private CargadorEscenario() {
    }

    public static SistemaAlojamiento cargar() {
        SistemaAlojamiento sistema = new SistemaAlojamiento("Aparta-Suites UQ");
        LocalDate hoy = LocalDate.now();

        // ---------------------------------------------------------- apartamentos
        Apartamento a101 = new Apartamento("A101", "Apto 101", "A", 1, 2, 4, "Vista jardin", 120000,
                EstadoOperativo.PREPARADO, null, LocalTime.of(15, 0));
        Apartamento a102 = new Apartamento("A102", "Apto 102", "A", 1, 2, 3, "Balcon", 110000,
                EstadoOperativo.OCUPADO, null, LocalTime.of(14, 30));
        Apartamento a201 = new Apartamento("A201", "Apto 201", "A", 2, 2, 4, "Terraza", 125000,
                EstadoOperativo.PENDIENTE_PREPARACION, null, LocalTime.of(15, 0));
        Apartamento a202 = new Apartamento("A202", "Apto 202", "A", 2, 1, 2, "Estudio", 90000,
                EstadoOperativo.FUERA_DE_SERVICIO, "Reparacion de tuberia", null);
        Apartamento b101 = new Apartamento("B101", "Apto B101", "B", 1, 2, 4, "Cerca de piscina", 130000,
                EstadoOperativo.PREPARADO, null, null);
        Apartamento b102 = new Apartamento("B102", "Apto B102", "B", 1, 1, 2, "Vista interior", 95000,
                EstadoOperativo.PENDIENTE_PREPARACION, null, null);
        Apartamento b201 = new Apartamento("B201", "Apto B201", "B", 2, 3, 5, "Suite", 145000,
                EstadoOperativo.PREPARADO, null, null);
        Apartamento b202 = new Apartamento("B202", "Apto B202", "B", 2, 2, 4, "Vista panoramica", 135000,
                EstadoOperativo.OCUPADO, null, null);

        for (Apartamento apto : new Apartamento[]{a101, a102, a201, a202, b101, b102, b201, b202}) {
            sistema.registrarApartamento(apto);
        }

        // -------------------------------------------------------------- huéspedes
        Huesped h1 = new Huesped("1001", "Ana Gómez", LocalDate.of(1988, 5, 14), "3100000001", "ana@example.com");
        Huesped h2 = new Huesped("1003", "Luis Pérez", LocalDate.of(1994, 7, 8), "3100000002", "luis@example.com");
        Huesped h3 = new Huesped("1002", "Carlos Ruiz", LocalDate.of(1990, 2, 21), "3100000003", "carlos@example.com");
        sistema.registrarHuesped(h1);
        sistema.registrarHuesped(h2);
        sistema.registrarHuesped(h3);

        // ---------------------------------------------------------- recepcionistas
        sistema.registrarRecepcionista(new Recepcionista("R1", "Sofía"));
        sistema.registrarRecepcionista(new Recepcionista("R2", "Mateo"));

        // -------------------------------------------------------------- temporadas
        sistema.registrarTemporada(new Temporada(TipoTemporada.BAJA, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31)));
        sistema.registrarTemporada(new Temporada(TipoTemporada.MEDIA, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 6, 30)));
        sistema.registrarTemporada(new Temporada(TipoTemporada.ALTA, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 31)));

        // ------------------------------------------------------------ mapa (grafo)
        // Recepción, cuarto de aseo y una escalera por bloque; los pesos son metros.
        sistema.registrarNodoMapa(new NodoEdificio("R", NodoEdificio.Tipo.RECEPCION, null));
        sistema.registrarNodoMapa(new NodoEdificio("ASEO", NodoEdificio.Tipo.SERVICIO, null));
        sistema.registrarNodoMapa(new NodoEdificio("ESC-A", NodoEdificio.Tipo.SERVICIO, null));
        sistema.registrarNodoMapa(new NodoEdificio("ESC-B", NodoEdificio.Tipo.SERVICIO, null));
        sistema.registrarNodoMapa(new NodoEdificio("A101", NodoEdificio.Tipo.APARTAMENTO, a101));
        sistema.registrarNodoMapa(new NodoEdificio("A102", NodoEdificio.Tipo.APARTAMENTO, a102));
        sistema.registrarNodoMapa(new NodoEdificio("A201", NodoEdificio.Tipo.APARTAMENTO, a201));
        sistema.registrarNodoMapa(new NodoEdificio("A202", NodoEdificio.Tipo.APARTAMENTO, a202));
        sistema.registrarNodoMapa(new NodoEdificio("B101", NodoEdificio.Tipo.APARTAMENTO, b101));
        sistema.registrarNodoMapa(new NodoEdificio("B102", NodoEdificio.Tipo.APARTAMENTO, b102));
        sistema.registrarNodoMapa(new NodoEdificio("B201", NodoEdificio.Tipo.APARTAMENTO, b201));
        sistema.registrarNodoMapa(new NodoEdificio("B202", NodoEdificio.Tipo.APARTAMENTO, b202));

        sistema.registrarPasillo("R", "ASEO", 8);
        sistema.registrarPasillo("R", "ESC-A", 12);
        sistema.registrarPasillo("R", "ESC-B", 25);
        sistema.registrarPasillo("R", "A101", 30);      // atajo largo: BFS lo elige, Dijkstra no
        sistema.registrarPasillo("ESC-A", "ESC-B", 40); // ruta alterna entre bloques
        sistema.registrarPasillo("ESC-A", "A101", 6);
        sistema.registrarPasillo("ESC-A", "A102", 9);
        sistema.registrarPasillo("ESC-A", "A201", 14);
        sistema.registrarPasillo("A101", "A102", 4);
        sistema.registrarPasillo("A201", "A202", 4);
        sistema.registrarPasillo("ESC-B", "B101", 6);
        sistema.registrarPasillo("ESC-B", "B102", 9);
        sistema.registrarPasillo("ESC-B", "B201", 14);
        sistema.registrarPasillo("B101", "B102", 4);
        sistema.registrarPasillo("B201", "B202", 4);

        // ----------------------------------------------------------------- reservas
        reservar(sistema, "RES-100", h1, a101, hoy.plusDays(2), hoy.plusDays(5),
                CanalReserva.DIRECTA, EstadoReserva.PENDIENTE, 2);
        reservar(sistema, "RES-101", h2, a102, hoy.minusDays(1), hoy.plusDays(2),
                CanalReserva.PORTAL, EstadoReserva.EN_CURSO, 2);
        reservar(sistema, "RES-102", h3, b202, hoy, hoy.plusDays(3),
                CanalReserva.EXTERNA, EstadoReserva.EN_CURSO, 2);
        reservar(sistema, "RES-103", h2, b201, hoy.plusDays(5), hoy.plusDays(8),
                CanalReserva.DIRECTA, EstadoReserva.CONFIRMADA, 3);
        reservar(sistema, "RES-104", h3, a201, hoy, hoy.plusDays(2),
                CanalReserva.PORTAL, EstadoReserva.CONFIRMADA, 2);

        // ------------------------------------------------------- cola de preparación
        // A201 tiene entrada hoy (prioridad 1); B102 no (prioridad 2).
        sistema.getColaPreparacion().encolar(new TareaPreparacion(b102, false, LocalTime.MAX));
        sistema.getColaPreparacion().encolar(new TareaPreparacion(a201, true, LocalTime.of(15, 0)));

        return sistema;
    }

    private static void reservar(SistemaAlojamiento sistema, String id, Huesped titular, Apartamento apto,
                                 LocalDate entrada, LocalDate salida, CanalReserva canal,
                                 EstadoReserva estado, int personas) {
        try {
            Estancia estancia = new Estancia(entrada, salida);
            Reserva reserva = new Reserva(id, titular, apto, estancia, canal, estado, LocalDateTime.now(), personas);
            apto.agregarReserva(reserva);
            sistema.getReservas().agregarFinal(reserva);
        } catch (Exception ignorada) {
            // Si una reserva de prueba no es válida, se omite y el resto del escenario se carga igual.
        }
    }
}