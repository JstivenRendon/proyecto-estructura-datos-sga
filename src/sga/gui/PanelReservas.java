package gui;

import comparadores.ComparadoresApartamento;
import estructuras.ListaEnlazada;
import excepciones.CapacidadExcedidaException;
import excepciones.FechasInvalidasException;
import excepciones.ReservaSolapadaException;
import excepciones.TransicionEstadoInvalidaException;
import modelo.Apartamento;
import modelo.CanalReserva;
import modelo.Estancia;
import modelo.NocheLiquidada;
import modelo.Reserva;
import modelo.TipoTemporada;
import servicios.SistemaAlojamiento;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;

public class PanelReservas extends JPanel {
    private SistemaAlojamiento sistema;
    private final JTextField campoDocumento = new JTextField("1001", 8);
    private final JTextField campoEntrada = new JTextField(LocalDate.now().plusDays(5).toString(), 10);
    private final JTextField campoSalida = new JTextField(LocalDate.now().plusDays(7).toString(), 10);
    private final JTextField campoPersonas = new JTextField("2", 3);
    private final JTextField campoIdReserva = new JTextField(10);
    private final JComboBox<String> selectorApartamento = new JComboBox<>();
    private final JComboBox<String> selectorOrden =
            new JComboBox<>(new String[]{"Precio", "Capacidad", "Dormitorios"});
    private final JComboBox<CanalReserva> selectorCanal =
            new JComboBox<>(new CanalReserva[]{CanalReserva.PORTAL, CanalReserva.DIRECTA});
    private final JTextArea areaResultado = new JTextArea();
    private CambioReservasListener cambioReservasListener;

    public PanelReservas(SistemaAlojamiento sistema) {
        if (sistema == null) {
            throw new IllegalArgumentException("El sistema de alojamiento no puede ser null");
        }
        this.sistema = sistema;
        construirInterfaz();
        buscarDisponibles();
    }

    public void setSistema(SistemaAlojamiento sistema) {
        if (sistema == null) {
            throw new IllegalArgumentException("El sistema de alojamiento no puede ser null");
        }
        this.sistema = sistema;
        buscarDisponibles();
    }

    public void setCambioReservasListener(CambioReservasListener listener) {
        this.cambioReservasListener = listener;
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel formulario = EstiloSGA.crearTarjeta(new GridLayout(0, 4, 8, 8));
        agregarCampo(formulario, "Documento", campoDocumento);
        agregarCampo(formulario, "Entrada", campoEntrada);
        agregarCampo(formulario, "Salida", campoSalida);
        agregarCampo(formulario, "Personas", campoPersonas);
        agregarCampo(formulario, "Orden", selectorOrden);
        agregarCampo(formulario, "Apartamento disponible", selectorApartamento);
        agregarCampo(formulario, "Canal", selectorCanal);

        JButton buscar = new JButton("Buscar disponibilidad");
        JButton crear = new JButton("Crear reserva");
        JButton pagar = new JButton("Pagar anticipo");
        EstiloSGA.estilizarBoton(buscar, false);
        EstiloSGA.estilizarBoton(crear, true);
        EstiloSGA.estilizarBoton(pagar, false);
        buscar.addActionListener(evento -> buscarDisponibles());
        crear.addActionListener(evento -> crearReserva());
        pagar.addActionListener(evento -> pagarAnticipo());

        JPanel accionesReserva = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 4));
        accionesReserva.setOpaque(false);
        accionesReserva.add(buscar);
        accionesReserva.add(crear);
        accionesReserva.add(pagar);
        formulario.add(accionesReserva);

        JPanel cancelar = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 4));
        cancelar.setOpaque(false);
        EstiloSGA.estilizarCampo(campoIdReserva);
        JButton cancelarReserva = new JButton("Cancelar reserva");
        JButton procesarVencimientos = new JButton("Procesar vencimientos");
        EstiloSGA.estilizarBoton(cancelarReserva, false);
        EstiloSGA.estilizarBoton(procesarVencimientos, false);
        cancelar.add(new JLabel("ID de reserva"));
        cancelar.add(campoIdReserva);
        cancelar.add(cancelarReserva);
        cancelar.add(procesarVencimientos);
        cancelarReserva.addActionListener(evento -> cancelarReserva());
        procesarVencimientos.addActionListener(evento -> procesarVencimientos());

        JPanel superior = new JPanel(new BorderLayout(0, 8));
        superior.setOpaque(false);
        superior.add(formulario, BorderLayout.NORTH);
        superior.add(cancelar, BorderLayout.SOUTH);
        add(superior, BorderLayout.NORTH);

        areaResultado.setEditable(false);
        areaResultado.setLineWrap(true);
        areaResultado.setWrapStyleWord(true);
        EstiloSGA.estilizarArea(areaResultado);
        add(new JScrollPane(areaResultado), BorderLayout.CENTER);

        for (JTextField campo : new JTextField[]{campoDocumento, campoEntrada, campoSalida, campoPersonas}) {
            EstiloSGA.estilizarCampo(campo);
        }
    }

    private void agregarCampo(JPanel formulario, String etiqueta, java.awt.Component campo) {
        formulario.add(new JLabel(etiqueta));
        formulario.add(campo);
    }

    private void buscarDisponibles() {
        try {
            Estancia estancia = obtenerEstancia();
            int personas = Integer.parseInt(campoPersonas.getText().trim());
            ListaEnlazada<Apartamento> disponibles =
                    sistema.buscarDisponibles(estancia, personas, obtenerComparador());
            selectorApartamento.removeAllItems();
            StringBuilder resultado = new StringBuilder("Apartamentos disponibles:\n");
            for (Apartamento apartamento : disponibles) {
                selectorApartamento.addItem(apartamento.getCodigo());
                resultado.append("• ").append(apartamento.getCodigo()).append(" — ")
                        .append(apartamento.getNombre()).append(" | capacidad ")
                        .append(apartamento.getCapacidadMaxima()).append(" | tarifa base ")
                        .append(apartamento.getTarifaBasePorPersona()).append('\n');
            }
            if (disponibles.estaVacia()) {
                resultado.append("No se encontraron apartamentos para esas fechas y ocupación.");
            }
            areaResultado.setText(resultado.toString());
        } catch (FechasInvalidasException | IllegalArgumentException error) {
            selectorApartamento.removeAllItems();
            areaResultado.setText("No se pudo consultar la disponibilidad: " + error.getMessage());
        }
    }

    private java.util.Comparator<Apartamento> obtenerComparador() {
        switch ((String) selectorOrden.getSelectedItem()) {
            case "Capacidad":
                return ComparadoresApartamento.porCapacidad();
            case "Dormitorios":
                return ComparadoresApartamento.porDormitorios();
            default:
                return ComparadoresApartamento.porPrecio(TipoTemporada.BAJA);
        }
    }

    private Estancia obtenerEstancia() throws FechasInvalidasException {
        return new Estancia(LocalDate.parse(campoEntrada.getText().trim()),
                LocalDate.parse(campoSalida.getText().trim()));
    }

    private void crearReserva() {
        Object codigoSeleccionado = selectorApartamento.getSelectedItem();
        if (codigoSeleccionado == null) {
            areaResultado.setText("Busque disponibilidad y seleccione un apartamento antes de reservar.");
            return;
        }
        try {
            int personas = Integer.parseInt(campoPersonas.getText().trim());
            Reserva reserva = sistema.crearReserva(campoDocumento.getText().trim(),
                    codigoSeleccionado.toString(), obtenerEstancia(),
                    (CanalReserva) selectorCanal.getSelectedItem(), personas);
            campoIdReserva.setText(reserva.getIdReserva());
            notificarCambioYActualizar("Reserva creada: " + reserva.getIdReserva()
                    + " (" + reserva.getEstado() + ").");
        } catch (FechasInvalidasException | ReservaSolapadaException | CapacidadExcedidaException
                | TransicionEstadoInvalidaException | IllegalArgumentException error) {
            areaResultado.setText("No se pudo crear la reserva: " + error.getMessage());
        }
    }

    private void pagarAnticipo() {
        try {
            Reserva reserva = sistema.buscarReservaPorId(campoIdReserva.getText().trim());
            if (reserva == null) {
                throw new IllegalArgumentException("Seleccione una reserva existente mediante su ID.");
            }
            double total = 0;
            for (NocheLiquidada noche : sistema.liquidar(
                    reserva.getApartamento(), reserva.getEstancia(), reserva)) {
                total += noche.getSubtotal();
            }
            sistema.pagarAnticipo(reserva, total);
            notificarCambioYActualizar("Anticipo registrado. Reserva " + reserva.getIdReserva()
                    + ": " + reserva.getEstado() + ".");
        } catch (TransicionEstadoInvalidaException | IllegalArgumentException error) {
            areaResultado.setText("No se pudo registrar el anticipo: " + error.getMessage());
        }
    }

    private void cancelarReserva() {
        try {
            Reserva reserva = sistema.buscarReservaPorId(campoIdReserva.getText().trim());
            if (reserva == null) {
                throw new IllegalArgumentException("No se encontró la reserva indicada.");
            }
            sistema.cancelar(reserva);
            notificarCambioYActualizar("Reserva cancelada: " + reserva.getIdReserva() + ".");
        } catch (TransicionEstadoInvalidaException | IllegalArgumentException error) {
            areaResultado.setText("No se pudo cancelar la reserva: " + error.getMessage());
        }
    }

    private void procesarVencimientos() {
        int vencidas = sistema.cancelarPendientesVencidas();
        notificarCambioYActualizar("Reservas pendientes vencidas y canceladas: " + vencidas + ".");
    }

    private void notificarCambioYActualizar(String mensaje) {
        if (cambioReservasListener != null) {
            cambioReservasListener.reservasCambiaron();
        }
        buscarDisponibles();
        areaResultado.append("\n\n" + mensaje);
    }
}
