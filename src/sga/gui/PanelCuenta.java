package gui;

import modelo.Folio;
import modelo.MovimientoFolio;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;

/**
 * Muestra el saldo y los movimientos de un folio.
 */
public class PanelCuenta extends JPanel implements EscuchaCambios {
    private final Folio folio;
    private final JLabel etiquetaSaldo = new JLabel();
    private final JTextArea movimientos = new JTextArea();

    public PanelCuenta(Folio folio) {
        this.folio = folio;
        setLayout(new BorderLayout());
        etiquetaSaldo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        movimientos.setEditable(false);
        add(etiquetaSaldo, BorderLayout.NORTH);
        add(new JScrollPane(movimientos), BorderLayout.CENTER);
        actualizar();
    }

    public void actualizar() {
        etiquetaSaldo.setText("Saldo del folio: " + folio.saldo());
        StringBuilder texto = new StringBuilder();
        for (MovimientoFolio movimiento : folio.getMovimientos()) {
            texto.append(movimiento).append("\n");
        }
        movimientos.setText(texto.toString());
    }

    @Override
    public void alCambiar() {
        actualizar();
    }
}