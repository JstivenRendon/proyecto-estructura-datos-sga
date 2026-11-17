package gui;

import modelo.MovimientoFolio;
import modelo.Reserva;
import servicios.SistemaAlojamiento;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class PanelCuenta extends JPanel implements EscuchaCambios {
    private SistemaAlojamiento sistema;
    private final JComboBox<String> selectorReserva = new JComboBox<>();
    private final JLabel etiquetaSaldo = new JLabel();
    private final JTextArea movimientos = new JTextArea();
    private boolean actualizandoSelector;

    public PanelCuenta(SistemaAlojamiento sistema) {
        if (sistema == null) {
            throw new IllegalArgumentException("El sistema de alojamiento no puede ser null");
        }
        this.sistema = sistema;
        setLayout(new BorderLayout(8, 8));
        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEADING));
        selector.add(new JLabel("Reserva:"));
        selector.add(selectorReserva);
        selector.add(etiquetaSaldo);
        selector.setOpaque(false);
        add(selector, BorderLayout.NORTH);

        movimientos.setEditable(false);
        EstiloSGA.estilizarArea(movimientos);
        add(new JScrollPane(movimientos), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        selectorReserva.addActionListener(evento -> {
            if (!actualizandoSelector) {
                actualizarDetalle();
            }
        });
        actualizar();
    }

    public void setSistema(SistemaAlojamiento sistema) {
        if (sistema == null) {
            throw new IllegalArgumentException("El sistema de alojamiento no puede ser null");
        }
        this.sistema = sistema;
        actualizar();
    }

    public Reserva getReservaSeleccionada() {
        Object id = selectorReserva.getSelectedItem();
        return id == null ? null : sistema.buscarReservaPorId(id.toString());
    }

    public void actualizar() {
        Object idAnterior = selectorReserva.getSelectedItem();
        actualizandoSelector = true;
        selectorReserva.removeAllItems();
        for (Reserva reserva : sistema.getReservas()) {
            if (reserva.getIdReserva() != null) {
                selectorReserva.addItem(reserva.getIdReserva());
            }
        }
        if (idAnterior != null) {
            selectorReserva.setSelectedItem(idAnterior);
        }
        actualizandoSelector = false;
        actualizarDetalle();
    }

    @Override
    public void alCambiar() {
        actualizar();
    }

    private void actualizarDetalle() {
        Reserva reserva = getReservaSeleccionada();
        if (reserva == null) {
            etiquetaSaldo.setText("Sin reserva seleccionada");
            movimientos.setText("No hay reservas registradas.");
            return;
        }
        etiquetaSaldo.setText("Saldo: " + reserva.getFolio().saldo());
        StringBuilder texto = new StringBuilder("Movimientos del folio ")
                .append(reserva.getIdReserva()).append(":\n");
        for (MovimientoFolio movimiento : reserva.getFolio().getMovimientos()) {
            texto.append(movimiento).append('\n');
        }
        movimientos.setText(texto.toString());
    }
}
