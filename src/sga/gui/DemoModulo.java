package gui;

import excepciones.FechasInvalidasException;
import gui.Demo.PanelCalendarioOcupacion;
import gui.Demo.PanelReservas;
import modelo.Apartamento;
import modelo.CanalReserva;
import modelo.EstadoOperativo;
import modelo.EstadoReserva;
import modelo.Estancia;
import modelo.Huesped;
import modelo.Reserva;
import modelo.Temporada;
import modelo.TipoTemporada;
import servicios.GestorReservas;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class DemoModulo {
    private DemoModulo() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                mostrarDemo();
            } catch (FechasInvalidasException error) {
                throw new IllegalStateException("No se pudo cargar el escenario de demostración", error);
            }
        });
    }

    private static void mostrarDemo() throws FechasInvalidasException {
        GestorReservas gestor = crearEscenario();
        PanelReservas panelReservas = new PanelReservas(gestor);
        PanelCalendarioOcupacion panelCalendario = new PanelCalendarioOcupacion(gestor);
        panelReservas.setCambioReservasListener(panelCalendario);

        LocalDate hoy = LocalDate.now();
        Temporada[] temporadas = {
            new Temporada(TipoTemporada.BAJA, LocalDate.of(hoy.getYear(), 1, 1),
                    LocalDate.of(hoy.getYear(), 4, 30)),
            new Temporada(TipoTemporada.MEDIA, LocalDate.of(hoy.getYear(), 5, 1),
                    LocalDate.of(hoy.getYear(), 8, 31)),
            new Temporada(TipoTemporada.ALTA, LocalDate.of(hoy.getYear(), 9, 1),
                    LocalDate.of(hoy.getYear(), 12, 31))
        };
        String textoTemporadas = "Temporadas: " + temporadas[0].getTipo() + " "
                + temporadas[0].getInicio() + "–" + temporadas[0].getFin()
                + " · " + temporadas[1].getTipo() + " " + temporadas[1].getInicio()
                + "–" + temporadas[1].getFin() + " · " + temporadas[2].getTipo()
                + " " + temporadas[2].getInicio() + "–" + temporadas[2].getFin();

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Reservas", panelReservas);
        pestanas.addTab("Calendario de ocupación", panelCalendario);

        JFrame ventana = new JFrame("SGA - Módulo Persona B: Reservas y Calendario");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setLayout(new BorderLayout());
        ventana.add(pestanas, BorderLayout.CENTER);
        ventana.add(new JLabel(textoTemporadas, SwingConstants.CENTER), BorderLayout.SOUTH);
        ventana.setSize(1250, 760);
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }

    private static GestorReservas crearEscenario() throws FechasInvalidasException {
        GestorReservas gestor = new GestorReservas();
        Apartamento[] apartamentos = {
            new Apartamento("A101", "Suite Jardín", "A", 1, 2, 2, "Vista al jardín",
                    120000, EstadoOperativo.PREPARADO, null, LocalTime.of(15, 0)),
            new Apartamento("A102", "Suite Balcón", "A", 1, 2, 4, "Balcón privado",
                    145000, EstadoOperativo.PREPARADO, null, LocalTime.of(16, 0)),
            new Apartamento("B201", "Suite Familiar", "B", 2, 3, 5, "Cocina equipada",
                    180000, EstadoOperativo.PREPARADO, null, null),
            new Apartamento("B202", "Suite Ejecutiva", "B", 2, 1, 2, "Escritorio",
                    110000, EstadoOperativo.PREPARADO, null, null),
            new Apartamento("C301", "Suite en mantenimiento", "C", 3, 2, 3, "Revisión eléctrica",
                    130000, EstadoOperativo.FUERA_DE_SERVICIO, "Mantenimiento preventivo", null)
        };
        for (Apartamento apartamento : apartamentos) {
            gestor.registrarApartamento(apartamento);
        }

        Huesped[] huespedes = {
            new Huesped("1001", "Ana Gómez", LocalDate.of(1990, 5, 14), "3100000001", "ana@example.com"),
            new Huesped("1002", "Luis Pérez", LocalDate.of(1985, 8, 2), "3100000002", "luis@example.com"),
            new Huesped("1003", "María Rojas", LocalDate.of(1998, 11, 21), "3100000003", "maria@example.com")
        };
        for (Huesped huesped : huespedes) {
            gestor.registrarHuesped(huesped);
        }

        LocalDate hoy = LocalDate.now();
        Reserva confirmada = new Reserva("RES-DEMO-1", huespedes[0], apartamentos[0],
                new Estancia(hoy.plusDays(2), hoy.plusDays(5)), CanalReserva.DIRECTA,
                EstadoReserva.CONFIRMADA, LocalDateTime.now().minusHours(2), 2);
        Reserva pendiente = new Reserva("RES-DEMO-2", huespedes[1], apartamentos[1],
                new Estancia(hoy.plusDays(4), hoy.plusDays(8)), CanalReserva.PORTAL,
                EstadoReserva.PENDIENTE, LocalDateTime.now().minusHours(23), 3);
        Reserva enCurso = new Reserva("RES-DEMO-3", huespedes[2], apartamentos[2],
                new Estancia(hoy.minusDays(1), hoy.plusDays(2)), CanalReserva.DIRECTA,
                EstadoReserva.EN_CURSO, LocalDateTime.now().minusDays(1), 2);
        gestor.registrarReservaInicial(confirmada);
        gestor.registrarReservaInicial(pendiente);
        gestor.registrarReservaInicial(enCurso);
        return gestor;
    }
}
