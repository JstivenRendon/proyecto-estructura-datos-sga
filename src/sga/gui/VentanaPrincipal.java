package gui;

import modelo.*;
import persistencia.CargadorEscenario;
import servicios.SistemaAlojamiento;

import javax.swing.*;
import java.awt.*;

public class VentanaPrincipal extends JFrame {
    private SistemaAlojamiento sistema;
    private final BusEventos busEventos = new BusEventos();
    private final JPanel tablero = new JPanel(new GridLayout(0, 2, 8, 8));
    private final JTextArea areaEstadisticas = new JTextArea();
    private final DefaultListModel<String> modeloPreparacion = new DefaultListModel<>();
    private PanelReservas panelReservas;
    private PanelCalendarioOcupacion panelCalendario;
    private VentanaRecepcion panelRecepcion;
    private PanelMapa panelMapa;

    public VentanaPrincipal() {
        super("SGA — Aparta-Suites UQ");
        EstiloSGA.aplicarTema();
        sistema = CargadorEscenario.cargar();
        panelReservas = new PanelReservas(sistema);
        panelCalendario = new PanelCalendarioOcupacion(sistema);
        panelRecepcion = new VentanaRecepcion(sistema);
        panelMapa = new PanelMapa(sistema);

        busEventos.suscribir(panelCalendario);
        busEventos.suscribir(panelRecepcion);
        busEventos.suscribir(panelMapa);
        panelReservas.setCambioReservasListener(this::notificarCambios);
        panelRecepcion.setCambioReservasListener(this::notificarCambios);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 680));
        setSize(1240, 800);
        setLocationRelativeTo(null);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Inicio", crearInicio());
        pestanas.addTab("Reservas", panelReservas);
        pestanas.addTab("Calendario", panelCalendario);
        pestanas.addTab("Recepción", panelRecepcion);
        pestanas.addTab("Preparación", crearPreparacion());
        pestanas.addTab("Mapa", crearMapa());
        pestanas.addTab("Estadísticas", crearEstadisticas());
        add(EstiloSGA.crearCabecera("Gestión de alojamiento",
                "Reservas, recepción y operación diaria en un solo sistema"), BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);
        refrescarTodo();
    }

    private JPanel crearInicio() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JLabel titulo = new JLabel("Tablero de apartamentos — " + sistema.getNombre(), SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        JButton cargar = new JButton("Restablecer datos iniciales");
        cargar.addActionListener(e -> {
            sistema = CargadorEscenario.cargar();
            panelReservas.setSistema(sistema);
            panelCalendario.setSistema(sistema);
            panelRecepcion.setSistema(sistema);
            panelMapa.setSistema(sistema);
            busEventos.notificar();
            refrescarTodo();
        });
        panel.add(titulo, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablero), BorderLayout.CENTER);
        panel.add(cargar, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return panel;
    }

    private JPanel crearPreparacion() {
        JPanel panel = new JPanel(new BorderLayout());
        JList<String> lista = new JList<>(modeloPreparacion);
        JButton despachar = new JButton("Despachar siguiente (prioridad)");
        JButton ruta = new JButton("Calcular ruta más corta");
        JLabel turno = new JLabel();
        despachar.addActionListener(e -> {
            try {
                TareaPreparacion t = sistema.despacharPreparacion();
                JOptionPane.showMessageDialog(this, "Preparado: " + t.getApartamento().getCodigo());
                notificarCambios();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });
        ruta.addActionListener(e -> {
            panelMapa.setRuta(sistema.rutaPreparacion());
            JOptionPane.showMessageDialog(this, "Ruta dibujada en la pestaña Mapa");
        });
        JPanel sur = new JPanel();
        sur.add(despachar);
        sur.add(ruta);
        JButton avanzarTurno = new JButton("Avanzar turno");
        avanzarTurno.addActionListener(evento -> {
            sistema.avanzarTurno();
            turno.setText("Turno: " + sistema.turnoActual());
            notificarCambios();
        });
        sur.add(avanzarTurno);
        turno.setText("Turno: " + sistema.turnoActual());
        sur.add(turno);
        panel.add(new JScrollPane(lista), BorderLayout.CENTER);
        panel.add(sur, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearMapa() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Verde Preparado · Azul Ocupado · Amarillo preparación · Rojo fuera de servicio",
                SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(panelMapa, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearEstadisticas() {
        JPanel panel = new JPanel(new BorderLayout());
        areaEstadisticas.setEditable(false);
        panel.add(new JScrollPane(areaEstadisticas), BorderLayout.CENTER);
        return panel;
    }

    private void refrescarTodo() {
        tablero.removeAll();
        for (Apartamento apto : sistema.getApartamentos()) {
            JButton boton = new JButton("<html><b>" + apto.getCodigo() + "</b><br>" + apto.getNombre()
                    + "<br>" + apto.getEstado() + "</html>");
            boton.setBackground(colorEstado(apto));
            boton.setOpaque(true);
            tablero.add(boton);
        }
        tablero.revalidate();
        tablero.repaint();

        modeloPreparacion.clear();
        for (TareaPreparacion tarea : sistema.getColaPreparacion()) {
            modeloPreparacion.addElement(tarea.toString());
        }
        panelMapa.repaint();

        StringBuilder est = new StringBuilder();
        est.append("Turno recepción: ").append(sistema.turnoActual()).append("\n");
        est.append("Reservas por canal:\n");
        int portal = 0, directa = 0, externa = 0, canceladas = 0;
        for (Reserva r : sistema.getReservas()) {
            if (r.getCanal() == CanalReserva.PORTAL) portal++;
            if (r.getCanal() == CanalReserva.DIRECTA) directa++;
            if (r.getCanal() == CanalReserva.EXTERNA) externa++;
            if (r.getEstado().name().contains("CANCEL")) canceladas++;
        }
        est.append(" Portal=").append(portal).append(" Directa=").append(directa)
                .append(" Externa=").append(externa).append(" Canceladas=").append(canceladas).append("\n");
        est.append("Conflictos de canal:\n");
        for (String c : sistema.getConflictosCanal()) {
            est.append(" - ").append(c).append("\n");
        }
        areaEstadisticas.setText(est.toString());
    }

    private void notificarCambios() {
        busEventos.notificar();
        refrescarTodo();
    }

    private Color colorEstado(Apartamento apto) {
        switch (apto.getEstado()) {
            case PREPARADO:
                return new Color(165, 214, 167);
            case OCUPADO:
                return new Color(144, 202, 249);
            case PENDIENTE_PREPARACION:
            case EN_PREPARACION:
                return new Color(255, 241, 118);
            default:
                return new Color(239, 154, 154);
        }
    }

}