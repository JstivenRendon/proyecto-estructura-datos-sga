package gui.Demo;

import excepciones.CapacidadExcedidaException;
import excepciones.FechasInvalidasException;
import excepciones.ReservaSolapadaException;
import excepciones.TransicionEstadoInvalidaException;
import modelo.CanalReserva;
import modelo.Estancia;
import modelo.Reserva;
import servicios.GestorReservas;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PanelReservas extends JPanel {
    private final GestorReservas gestorReservas;
    private final JTextField campoDocumento = new JTextField("1001", 8);
    private final JTextField campoApartamento = new JTextField("A101", 6);
    private final JTextField campoEntrada = new JTextField(LocalDate.now().plusDays(5).toString(), 10);
    private final JTextField campoSalida = new JTextField(LocalDate.now().plusDays(7).toString(), 10);
    private final JTextField campoIdCancelacion = new JTextField(10);
    private final JComboBox<CanalReserva> selectorCanal = new JComboBox<>(CanalReserva.values());
    private final JTextArea areaResultado = new JTextArea();
    private CambioReservasListener cambioReservasListener;

    public PanelReservas(GestorReservas gestorReservas) {
        if (gestorReservas == null) {
            throw new IllegalArgumentException("El gestor de reservas no puede ser null");
        }
        this.gestorReservas = gestorReservas;
        construirInterfaz();
    }

    public void setCambioReservasListener(CambioReservasListener listener) {
        this.cambioReservasListener = listener;
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(8, 8));
        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEADING));
        formulario.add(new JLabel("Documento:"));
        formulario.add(campoDocumento);
        formulario.add(new JLabel("Apartamento:"));
        formulario.add(campoApartamento);
        formulario.add(new JLabel("Entrada:"));
        formulario.add(campoEntrada);
        formulario.add(new JLabel("Salida:"));
        formulario.add(campoSalida);
        formulario.add(selectorCanal);

        JButton crear = new JButton("Crear reserva");
        crear.addActionListener(evento -> crearReserva());
        formulario.add(crear);

        JButton vencer = new JButton("Procesar vencimientos");
        vencer.addActionListener(evento -> procesarVencimientos());
        formulario.add(vencer);

        JPanel cancelar = new JPanel(new FlowLayout(FlowLayout.LEADING));
        cancelar.add(new JLabel("ID de reserva:"));
        cancelar.add(campoIdCancelacion);
        JButton cancelarReserva = new JButton("Cancelar");
        cancelarReserva.addActionListener(evento -> cancelarReserva());
        cancelar.add(cancelarReserva);

        JPanel superior = new JPanel(new BorderLayout());
        superior.add(formulario, BorderLayout.NORTH);
        superior.add(cancelar, BorderLayout.SOUTH);
        add(superior, BorderLayout.NORTH);

        areaResultado.setEditable(false);
        areaResultado.setLineWrap(true);
        areaResultado.setWrapStyleWord(true);
        areaResultado.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(new JScrollPane(areaResultado), BorderLayout.CENTER);
    }

    private void crearReserva() {
        try {
            Estancia estancia = new Estancia(
                    LocalDate.parse(campoEntrada.getText().trim()),
                    LocalDate.parse(campoSalida.getText().trim()));
            Reserva reserva = gestorReservas.crearReserva(
                    campoDocumento.getText().trim(),
                    campoApartamento.getText().trim(),
                    estancia,
                    (CanalReserva) selectorCanal.getSelectedItem());
            campoIdCancelacion.setText(reserva.getIdReserva());
            areaResultado.setText("Reserva creada: " + reserva.getIdReserva()
                    + " (" + reserva.getEstado() + ")");
            notificarCambio();
        } catch (FechasInvalidasException | ReservaSolapadaException | CapacidadExcedidaException
                | TransicionEstadoInvalidaException | IllegalArgumentException error) {
            areaResultado.setText("No se pudo crear la reserva: " + error.getMessage());
        }
    }

    private void cancelarReserva() {
        try {
            double reembolso = gestorReservas.cancelarReserva(
                    campoIdCancelacion.getText().trim(), LocalDateTime.now());
            areaResultado.setText("Reserva cancelada. Reembolso calculado: " + reembolso);
            notificarCambio();
        } catch (TransicionEstadoInvalidaException | IllegalArgumentException error) {
            areaResultado.setText("No se pudo cancelar la reserva: " + error.getMessage());
        }
    }

    private void procesarVencimientos() {
        try {
            int vencidas = gestorReservas.procesarVencimientos(LocalDateTime.now());
            areaResultado.setText("Reservas pendientes vencidas y canceladas: " + vencidas);
            if (vencidas > 0) {
                notificarCambio();
            }
        } catch (TransicionEstadoInvalidaException error) {
            areaResultado.setText("No se pudieron procesar los vencimientos: " + error.getMessage());
        }
    }

    private void notificarCambio() {
        if (cambioReservasListener != null) {
            cambioReservasListener.reservasCambiaron();
        }
    }
}
