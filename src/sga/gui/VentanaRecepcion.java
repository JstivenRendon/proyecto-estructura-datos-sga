package sga.gui;

import modelo.Folio;
import modelo.Huesped;
import modelo.MovimientoFolio;
import modelo.Recepcionista;
import modelo.SolicitudExterna;
import servicios.ServicioRecepcion;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.time.LocalDate;

/**
 * Interfaz de la persona A: panel de huésped, folio con deshacer y canal externo FIFO.
 */
public class VentanaRecepcion extends JFrame {
    private final ServicioRecepcion recepcion = new ServicioRecepcion();
    private final Huesped huespedDemo = new Huesped(
            "1001", "Ana Pérez", LocalDate.of(1995, 4, 12), "3001112233", "ana@mail.com");
    private final Folio folioDemo = new Folio();
    private final JTextArea areaHuesped = new JTextArea();
    private final JTextArea areaFolio = new JTextArea();
    private final JTextArea areaExterno = new JTextArea();
    private final JLabel etiquetaTurno = new JLabel();

    public VentanaRecepcion() {
        super("SGA — Recepción (persona A)");
        recepcion.registrarRecepcionista(new Recepcionista("R1", "Marta"));
        recepcion.registrarRecepcionista(new Recepcionista("R2", "Julián"));
        recepcion.registrarRecepcionista(new Recepcionista("R3", "Sofía"));
        recepcion.encolarExterna(new SolicitudExterna(
                "Booking", "1002", "A101", LocalDate.now().plusDays(2), LocalDate.now().plusDays(5), 2));
        recepcion.encolarExterna(new SolicitudExterna(
                "Airbnb", "1003", "B201", LocalDate.now().plusDays(10), LocalDate.now().plusDays(12), 2));
        huespedDemo.agregarAlHistorial("A101 · 3 noches · folio abierto");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Huésped", crearPanelHuesped());
        pestanas.addTab("Folio", crearPanelFolio());
        pestanas.addTab("Canal externo", crearPanelExterno());
        add(pestanas);
        refrescar();
    }

    private JPanel crearPanelHuesped() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel norte = new JPanel();
        JTextField codigo = new JTextField("A102", 8);
        JButton favorito = new JButton("Agregar a favoritos");
        favorito.addActionListener(e -> {
            boolean nuevo = huespedDemo.agregarFavorito(codigo.getText().trim());
            if (!nuevo) {
                JOptionPane.showMessageDialog(this, "Ese apartamento ya estaba en favoritos (conjunto sin repetidos).");
            }
            refrescar();
        });
        norte.add(new JLabel("Código apto:"));
        norte.add(codigo);
        norte.add(favorito);
        areaHuesped.setEditable(false);
        panel.add(norte, BorderLayout.NORTH);
        panel.add(new JScrollPane(areaHuesped), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelFolio() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel norte = new JPanel();
        JButton cargo = new JButton("Cargo minibar $25000");
        JButton pago = new JButton("Pago $25000");
        JButton deshacer = new JButton("Deshacer última operación");
        cargo.addActionListener(e -> {
            folioDemo.registrarCargo("Minibar", 25000);
            refrescar();
        });
        pago.addActionListener(e -> {
            folioDemo.registrarPago("Abono", 25000);
            refrescar();
        });
        deshacer.addActionListener(e -> {
            try {
                folioDemo.deshacerUltimaOperacion();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
            refrescar();
        });
        norte.add(cargo);
        norte.add(pago);
        norte.add(deshacer);
        areaFolio.setEditable(false);
        panel.add(norte, BorderLayout.NORTH);
        panel.add(new JScrollPane(areaFolio), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelExterno() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel norte = new JPanel();
        JButton procesar = new JButton("Procesar siguiente (FIFO)");
        JButton turno = new JButton("Avanzar turno");
        procesar.addActionListener(e -> {
            try {
                JOptionPane.showMessageDialog(this, recepcion.procesarSiguienteExterna());
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
            refrescar();
        });
        turno.addActionListener(e -> {
            recepcion.avanzarTurno();
            refrescar();
        });
        norte.add(etiquetaTurno);
        norte.add(procesar);
        norte.add(turno);
        areaExterno.setEditable(false);
        panel.add(norte, BorderLayout.NORTH);
        panel.add(new JScrollPane(areaExterno), BorderLayout.CENTER);
        return panel;
    }

    private void refrescar() {
        StringBuilder huesped = new StringBuilder(huespedDemo + "\nFavoritos:\n");
        for (String fav : huespedDemo.getFavoritos()) {
            huesped.append(" - ").append(fav).append("\n");
        }
        huesped.append("Historial:\n");
        for (String item : huespedDemo.getHistorial()) {
            huesped.append(" - ").append(item).append("\n");
        }
        areaHuesped.setText(huesped.toString());

        StringBuilder folio = new StringBuilder("Saldo: ").append(folioDemo.saldo()).append("\nMovimientos (no se borran):\n");
        for (MovimientoFolio movimiento : folioDemo.getMovimientos()) {
            folio.append(" - ").append(movimiento).append("\n");
        }
        areaFolio.setText(folio.toString());

        etiquetaTurno.setText("Turno: " + recepcion.turnoActual());
        StringBuilder cola = new StringBuilder("Cola externa (el primero es el más antiguo):\n");
        for (SolicitudExterna solicitud : recepcion.getCanalExterno()) {
            cola.append(" - ").append(solicitud).append("\n");
        }
        if (recepcion.getCanalExterno().estaVacia()) {
            cola.append(" (vacía)\n");
        }
        areaExterno.setText(cola.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaRecepcion().setVisible(true));
    }
}

