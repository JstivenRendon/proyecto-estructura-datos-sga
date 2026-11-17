package gui;

import comparadores.ComparadoresApartamento;
import estructuras.ListaEnlazada;
import excepciones.FechasInvalidasException;
import excepciones.TransicionEstadoInvalidaException;
import modelo.*;
import persistencia.CargadorEscenario;
import persistencia.LectorEscenario;
import servicios.ServicioEstadisticas;
import servicios.SistemaAlojamiento;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;

public class VentanaPrincipal extends JFrame {
    private static final String ARCHIVO_ESCENARIO = "datos/escenario.txt";

    private SistemaAlojamiento sistema;
    private final JPanel tablero = new JPanel(new GridLayout(0, 2, 8, 8));
    private final JTextArea areaReservas = new JTextArea();
    private final JTextArea areaCalendario = new JTextArea();
    private final JTextArea areaUsuario = new JTextArea();
    private final JTextArea areaEstadisticas = new JTextArea();
    private final DefaultListModel<String> modeloPreparacion = new DefaultListModel<>();
    private final JTextArea areaRuta = new JTextArea(3, 40);
    private PanelMapa panelMapa;

    public VentanaPrincipal() {
        super("SGA — Aparta-Suites UQ");
        this.sistema = leerEscenarioInicial();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Inicio", crearInicio());
        pestanas.addTab("Reservas", crearReservas());
        pestanas.addTab("Calendario", crearCalendario());
        pestanas.addTab("Huésped", crearUsuario());
        pestanas.addTab("Preparación", crearPreparacion());
        pestanas.addTab("Mapa", crearMapa());
        pestanas.addTab("Estadísticas", crearEstadisticas());
        add(pestanas);
        refrescarTodo();
    }

    /**
     * Al abrir la aplicación se intenta leer el archivo; si no existe o tiene
     * errores se usa el escenario que está escrito en el código.
     */
    private SistemaAlojamiento leerEscenarioInicial() {
        try {
            return LectorEscenario.cargar(ARCHIVO_ESCENARIO);
        } catch (Exception ex) {
            return CargadorEscenario.cargar();
        }
    }

    private JPanel crearInicio() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JLabel titulo = new JLabel("Tablero de apartamentos — " + sistema.getNombre(), SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        JButton cargar = new JButton("Cargar escenario de prueba (" + ARCHIVO_ESCENARIO + ")");
        cargar.addActionListener(e -> {
            try {
                sistema = LectorEscenario.cargar(ARCHIVO_ESCENARIO);
                panelMapa.setSistema(sistema);
                refrescarTodo();
                areaRuta.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se pudo cargar el escenario:\n" + ex.getMessage());
            }
        });
        panel.add(titulo, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablero), BorderLayout.CENTER);
        panel.add(cargar, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return panel;
    }

    private JPanel crearReservas() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField doc = new JTextField("1003", 8);
        JTextField codigo = new JTextField("A102", 6);
        JTextField entrada = new JTextField(LocalDate.now().plusDays(7).toString(), 10);
        JTextField salida = new JTextField(LocalDate.now().plusDays(10).toString(), 10);
        JTextField personas = new JTextField("2", 3);
        JComboBox<String> orden = new JComboBox<>(new String[]{"precio", "capacidad", "dormitorios"});
        JButton buscar = new JButton("Buscar disponibles");
        JButton crearDirecta = new JButton("Reserva directa");
        JButton crearPortal = new JButton("Reserva portal + anticipo");
        JButton externa = new JButton("Procesar canal externo");
        form.add(new JLabel("Doc:"));
        form.add(doc);
        form.add(new JLabel("Apto:"));
        form.add(codigo);
        form.add(new JLabel("Entrada:"));
        form.add(entrada);
        form.add(new JLabel("Salida:"));
        form.add(salida);
        form.add(new JLabel("Pers:"));
        form.add(personas);
        form.add(orden);
        form.add(buscar);
        areaReservas.setEditable(false);
        buscar.addActionListener(e -> buscarDisponibles(entrada.getText(), salida.getText(), personas.getText(), (String) orden.getSelectedItem()));
        crearDirecta.addActionListener(e -> crearReserva(doc.getText(), codigo.getText(), entrada.getText(), salida.getText(), CanalReserva.DIRECTA, false));
        crearPortal.addActionListener(e -> crearReserva(doc.getText(), codigo.getText(), entrada.getText(), salida.getText(), CanalReserva.PORTAL, true));
        externa.addActionListener(e -> {
            try {
                JOptionPane.showMessageDialog(this, sistema.procesarSiguienteExterna());
                refrescarTodo();
            } catch (FechasInvalidasException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });
        JButton checkIn = new JButton("Check-in");
        JButton checkOut = new JButton("Check-out");
        checkIn.addActionListener(e -> registrarCheck(true));
        checkOut.addActionListener(e -> registrarCheck(false));
        JPanel llegadasSalidas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        llegadasSalidas.add(new JLabel("Acciones:"));
        llegadasSalidas.add(crearDirecta);
        llegadasSalidas.add(crearPortal);
        llegadasSalidas.add(externa);
        llegadasSalidas.add(checkIn);
        llegadasSalidas.add(checkOut);

        JPanel norte = new JPanel(new GridLayout(0, 1));
        norte.add(form);
        norte.add(llegadasSalidas);
        panel.add(norte, BorderLayout.NORTH);
        panel.add(new JScrollPane(areaReservas), BorderLayout.CENTER);
        return panel;
    }

    /**
     * Check-in (reservas Confirmadas) o check-out (reservas En curso).
     * El check-in solo procede si el apartamento está Preparado; el check-out deja
     * el apartamento Pendiente de preparación y lo mete a la cola de aseo.
     */
    private void registrarCheck(boolean esCheckIn) {
        EstadoReserva estadoNecesario = esCheckIn ? EstadoReserva.CONFIRMADA : EstadoReserva.EN_CURSO;
        int cantidad = 0;
        for (Reserva reserva : sistema.getReservas()) {
            if (reserva.getEstado() == estadoNecesario) {
                cantidad++;
            }
        }
        if (cantidad == 0) {
            JOptionPane.showMessageDialog(this, esCheckIn
                    ? "No hay reservas confirmadas para hacer check-in"
                    : "No hay reservas en curso para hacer check-out");
            return;
        }
        Reserva[] candidatas = new Reserva[cantidad];
        String[] etiquetas = new String[cantidad];
        int i = 0;
        for (Reserva reserva : sistema.getReservas()) {
            if (reserva.getEstado() == estadoNecesario) {
                candidatas[i] = reserva;
                etiquetas[i] = reserva + " | " + reserva.getApartamento().getCodigo() + " | "
                        + reserva.getEstancia() + " | " + reserva.getTitular().getNombre();
                i++;
            }
        }
        String elegida = (String) JOptionPane.showInputDialog(this, "Elija la reserva:",
                esCheckIn ? "Check-in" : "Check-out", JOptionPane.QUESTION_MESSAGE, null, etiquetas, etiquetas[0]);
        if (elegida == null) {
            return;
        }
        Reserva reserva = null;
        for (int k = 0; k < cantidad; k++) {
            if (etiquetas[k].equals(elegida)) {
                reserva = candidatas[k];
            }
        }
        try {
            if (esCheckIn) {
                sistema.checkIn(reserva);
                areaReservas.setText("Check-in realizado: " + reserva.getApartamento().getCodigo() + " queda Ocupado");
            } else {
                sistema.checkOut(reserva);
                areaReservas.setText("Check-out realizado: " + reserva.getApartamento().getCodigo()
                        + " queda Pendiente de preparación y entra a la cola de aseo");
            }
            refrescarTodo();
        } catch (TransicionEstadoInvalidaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private JPanel crearCalendario() {
        JPanel panel = new JPanel(new BorderLayout());
        areaCalendario.setFont(new Font("Monospaced", Font.PLAIN, 13));
        areaCalendario.setEditable(false);
        panel.add(new JScrollPane(areaCalendario), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearUsuario() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel norte = new JPanel();
        JTextField doc = new JTextField("1001", 10);
        JButton ver = new JButton("Ver panel del huésped");
        JButton fav = new JButton("Agregar A102 a favoritos");
        norte.add(new JLabel("Documento:"));
        norte.add(doc);
        norte.add(ver);
        norte.add(fav);
        areaUsuario.setEditable(false);
        ver.addActionListener(e -> mostrarUsuario(doc.getText()));
        fav.addActionListener(e -> {
            Huesped h = sistema.buscarHuesped(doc.getText().trim());
            if (h != null) {
                h.agregarFavorito("A102");
                mostrarUsuario(doc.getText());
            }
        });
        panel.add(norte, BorderLayout.NORTH);
        panel.add(new JScrollPane(areaUsuario), BorderLayout.CENTER);
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
                refrescarTodo();
                areaRuta.setText("");
                panelMapa.setRuta(null);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });
        ruta.addActionListener(e -> {
            ListaEnlazada<NodoEdificio> rutaCalculada = sistema.rutaPreparacion();
            panelMapa.setRuta(rutaCalculada);
            areaRuta.setText(textoRuta(rutaCalculada));
        });
        JPanel sur = new JPanel();
        sur.add(despachar);
        sur.add(ruta);
        sur.add(new JButton("Avanzar turno") {{
            addActionListener(ev -> {
                sistema.avanzarTurno();
                turno.setText("Turno: " + sistema.turnoActual());
            });
        }});
        turno.setText("Turno: " + sistema.turnoActual());
        sur.add(turno);
        areaRuta.setEditable(false);
        areaRuta.setLineWrap(true);
        areaRuta.setWrapStyleWord(true);
        panel.add(new JScrollPane(areaRuta), BorderLayout.NORTH);
        panel.add(new JScrollPane(lista), BorderLayout.CENTER);
        panel.add(sur, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearMapa() {
        panelMapa = new PanelMapa(sistema);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Verde Preparado · Azul Ocupado · Amarillo preparación · Rojo fuera de servicio",
                SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(panelMapa, BorderLayout.CENTER);

        JButton agrupar = new JButton("Agrupar apartamentos por bloque (DFS)");
        agrupar.addActionListener(e -> JOptionPane.showMessageDialog(this, sistema.reporteBloques()));
        JPanel sur = new JPanel();
        sur.add(agrupar);
        panel.add(sur, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearEstadisticas() {
        JPanel panel = new JPanel(new BorderLayout());
        areaEstadisticas.setFont(new Font("Monospaced", Font.PLAIN, 13));
        areaEstadisticas.setEditable(false);
        panel.add(new JScrollPane(areaEstadisticas), BorderLayout.CENTER);
        return panel;
    }

    private void buscarDisponibles(String entrada, String salida, String personasTxt, String criterio) {
        try {
            Estancia estancia = new Estancia(LocalDate.parse(entrada.trim()), LocalDate.parse(salida.trim()));
            int personas = Integer.parseInt(personasTxt.trim());
            Comparator<Apartamento> orden;
            switch (criterio) {
                case "capacidad":
                    orden = ComparadoresApartamento.porCapacidad();
                    break;
                case "dormitorios":
                    orden = ComparadoresApartamento.porDormitorios();
                    break;
                default:
                    orden = ComparadoresApartamento.porPrecio(TipoTemporada.BAJA);
            }
            ListaEnlazada<Apartamento> lista = sistema.buscarDisponibles(estancia, personas, orden);
            StringBuilder sb = new StringBuilder("Disponibles:\n");
            for (Apartamento apto : lista) {
                sb.append(" - ").append(apto).append(" cap=").append(apto.getCapacidadMaxima())
                        .append(" tarifa baja=").append(apto.tarifa(TipoTemporada.BAJA)).append("\n");
            }
            areaReservas.setText(sb.toString());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void crearReserva(String doc, String codigo, String entrada, String salida, CanalReserva canal, boolean anticipo) {
        try {
            Estancia estancia = new Estancia(LocalDate.parse(entrada.trim()), LocalDate.parse(salida.trim()));
            Reserva reserva = sistema.crearReserva(doc.trim(), codigo.trim(), estancia, canal);
            if (anticipo) {
                ListaEnlazada<NocheLiquidada> desglose = sistema.liquidar(reserva.getApartamento(), estancia, reserva);
                double total = 0;
                StringBuilder sb = new StringBuilder("Desglose:\n");
                for (NocheLiquidada noche : desglose) {
                    sb.append(noche).append("\n");
                    total += noche.getSubtotal();
                }
                sistema.pagarAnticipo(reserva, total);
                sb.append("Total: ").append(total).append("\nAnticipo 30% cobrado. Reserva: ").append(reserva);
                areaReservas.setText(sb.toString());
            } else {
                areaReservas.setText("Reserva creada:\n" + reserva);
            }
            refrescarTodo();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private String textoRuta(ListaEnlazada<NodoEdificio> ruta) {
        if (ruta.estaVacia()) {
            return "No hay apartamentos pendientes de preparación";
        }
        StringBuilder sb = new StringBuilder("Ruta: ");
        boolean primero = true;
        for (NodoEdificio nodo : ruta) {
            if (!primero) {
                sb.append(" -> ");
            }
            sb.append(nodo.getId());
            primero = false;
        }
        sb.append("\nDistancia total: ").append(String.format("%.0f", sistema.distanciaRuta(ruta))).append(" m");
        return sb.toString();
    }

    private void mostrarUsuario(String documento) {
        Huesped h = sistema.buscarHuesped(documento.trim());
        if (h == null) {
            areaUsuario.setText("Huésped no encontrado");
            return;
        }
        StringBuilder sb = new StringBuilder(h + "\nFavoritos:\n");
        for (String fav : h.getFavoritos()) {
            sb.append(" - ").append(fav).append("\n");
        }
        sb.append("Historial:\n");
        for (Object item : h.getHistorial()) {
            if (item instanceof Reserva) {
                Reserva r = (Reserva) item;
                sb.append(" - ").append(r).append(" saldo folio=").append(r.getFolio().saldo()).append("\n");
            } else {
                sb.append(" - ").append(item).append("\n");
            }
        }
        areaUsuario.setText(sb.toString());
    }

    private void refrescarTodo() {
        tablero.removeAll();
        for (Apartamento apto : sistema.getApartamentos()) {
            JButton boton = new JButton("<html><b>" + apto.getCodigo() + "</b><br>" + apto.getNombre()
                    + "<br>" + apto.getEstado() + "</html>");
            boton.setBackground(colorEstado(apto));
            boton.setOpaque(true);
            boton.addActionListener(e -> mostrarDetalleApartamento(apto));
            tablero.add(boton);
        }
        tablero.revalidate();
        tablero.repaint();

        StringBuilder cal = new StringBuilder(String.format("%-8s", "Apto"));
        LocalDate inicio = LocalDate.now();
        for (int i = 0; i < 14; i++) {
            cal.append(String.format("%6s", inicio.plusDays(i).getDayOfMonth()));
        }
        cal.append("\n");
        for (Apartamento apto : sistema.getApartamentos()) {
            cal.append(String.format("%-8s", apto.getCodigo()));
            for (int i = 0; i < 14; i++) {
                LocalDate noche = inicio.plusDays(i);
                boolean ocupada = false;
                try {
                    ocupada = apto.haySolapamiento(new Estancia(noche, noche.plusDays(1)));
                } catch (FechasInvalidasException ignored) {
                }
                cal.append(String.format("%6s", ocupada ? "██" : "·"));
            }
            cal.append("\n");
        }
        areaCalendario.setText(cal.toString());

        modeloPreparacion.clear();
        int posicion = 1;
        for (TareaPreparacion tarea : sistema.getColaPreparacion()) {
            String texto = posicion + ". " + tarea;
            if (tarea.isEntradaHoy()) {
                texto = texto + " (Prioridad 1, llega a las " + tarea.getHoraLlegada() + ")";
            } else {
                texto = texto + " (Prioridad 2)";
            }
            modeloPreparacion.addElement(texto);
            posicion++;
        }
        if (panelMapa != null) {
            panelMapa.repaint();
        }

        LocalDate hoy = LocalDate.now();
        ServicioEstadisticas estadisticas = new ServicioEstadisticas(sistema, hoy.minusDays(15), hoy.plusDays(15));
        areaEstadisticas.setText("Turno recepción: " + sistema.turnoActual() + "\n\n" + estadisticas.generarReporte());
    }

    /** Al tocar una tarjeta del tablero se muestran los datos y las reservas del apartamento. */
    private void mostrarDetalleApartamento(Apartamento apto) {
        StringBuilder sb = new StringBuilder();
        sb.append(apto.getCodigo()).append(" - ").append(apto.getNombre()).append("\n");
        sb.append("Bloque ").append(apto.getBloque()).append(", piso ").append(apto.getPiso()).append("\n");
        sb.append("Dormitorios: ").append(apto.getNumeroDormitorios())
                .append(" | Capacidad: ").append(apto.getCapacidadMaxima()).append(" personas\n");
        sb.append("Características: ").append(apto.getCaracteristicas()).append("\n");
        sb.append(String.format("Tarifa base: $%,.0f por persona por noche%n", apto.getTarifaBasePorPersona()));
        sb.append("Estado: ").append(apto.getEstado()).append("\n");
        if (apto.getMotivoFueraDeServicio() != null) {
            sb.append("Motivo fuera de servicio: ").append(apto.getMotivoFueraDeServicio()).append("\n");
        }
        sb.append("\nReservas (ordenadas por fecha de entrada):\n");
        boolean hay = false;
        for (Reserva reserva : apto.getReservas()) {
            hay = true;
            sb.append("  ").append(reserva.getIdReserva()).append(" | ")
                    .append(reserva.getEstancia().getFechaEntrada()).append(" a ")
                    .append(reserva.getEstancia().getFechaSalida()).append(" | ")
                    .append(reserva.getEstado()).append(" | ")
                    .append(reserva.getTitular().getNombre()).append("\n");
        }
        if (!hay) {
            sb.append("  (sin reservas)\n");
        }
        boolean fuera = apto.getEstado() == EstadoApartamento.FUERA_DE_SERVICIO;
        String[] opciones = {"Cerrar", fuera ? "Volver a servicio" : "Poner fuera de servicio"};
        int elegida = JOptionPane.showOptionDialog(this, sb.toString(), "Apartamento " + apto.getCodigo(),
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opciones, opciones[0]);
        if (elegida != 1) {
            return;
        }
        try {
            if (fuera) {
                // Al volver a servicio queda Pendiente de preparación y entra a la cola de aseo
                apto.cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION);
                sistema.getColaPreparacion().encolar(new TareaPreparacion(apto, false, LocalTime.MAX));
            } else {
                String motivo = JOptionPane.showInputDialog(this, "Motivo (obligatorio):");
                if (motivo == null) {
                    return;
                }
                apto.ponerFueraDeServicio(motivo);
            }
            refrescarTodo();
        } catch (TransicionEstadoInvalidaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
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

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}