package gui;

import excepciones.FechasInvalidasException;
import modelo.Apartamento;
import modelo.Huesped;
import modelo.Reserva;
import modelo.SolicitudExterna;
import servicios.SistemaAlojamiento;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;

public class VentanaRecepcion extends JPanel implements EscuchaCambios {
    private SistemaAlojamiento sistema;
    private CambioReservasListener cambioReservasListener;
    private final JTextField campoDocumento = new JTextField("1001", 10);
    private final JTextField campoCodigoFavorito = new JTextField("A102", 8);
    private final JTextArea areaHuesped = new JTextArea();
    private final JTextArea areaExterno = new JTextArea();
    private final JLabel etiquetaTurno = new JLabel();
    private final PanelCuenta panelCuenta;

    public VentanaRecepcion(SistemaAlojamiento sistema) {
        if (sistema == null) {
            throw new IllegalArgumentException("El sistema de alojamiento no puede ser null");
        }
        this.sistema = sistema;
        this.panelCuenta = new PanelCuenta(sistema);

        setLayout(new BorderLayout());
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Huésped", crearPanelHuesped());
        pestanas.addTab("Folio", crearPanelFolio());
        pestanas.addTab("Canal externo", crearPanelExterno());
        add(pestanas, BorderLayout.CENTER);
        refrescar();
    }

    public void setSistema(SistemaAlojamiento sistema) {
        if (sistema == null) {
            throw new IllegalArgumentException("El sistema de alojamiento no puede ser null");
        }
        this.sistema = sistema;
        panelCuenta.setSistema(sistema);
        refrescar();
    }

    public void setCambioReservasListener(CambioReservasListener listener) {
        this.cambioReservasListener = listener;
    }

    @Override
    public void alCambiar() {
        refrescar();
    }

    private JPanel crearPanelHuesped() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel norte = new JPanel(new FlowLayout(FlowLayout.LEADING));
        EstiloSGA.estilizarCampo(campoDocumento);
        EstiloSGA.estilizarCampo(campoCodigoFavorito);
        JButton ver = new JButton("Consultar huésped");
        JButton favorito = new JButton("Agregar favorito");
        EstiloSGA.estilizarBoton(ver, false);
        EstiloSGA.estilizarBoton(favorito, true);
        ver.addActionListener(evento -> refrescarHuesped());
        favorito.addActionListener(evento -> agregarFavorito());
        norte.add(new JLabel("Documento:"));
        norte.add(campoDocumento);
        norte.add(ver);
        norte.add(new JLabel("Código de apartamento:"));
        norte.add(campoCodigoFavorito);
        norte.add(favorito);
        areaHuesped.setEditable(false);
        EstiloSGA.estilizarArea(areaHuesped);
        panel.add(norte, BorderLayout.NORTH);
        panel.add(new JScrollPane(areaHuesped), BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return panel;
    }

    private JPanel crearPanelFolio() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEADING));
        JButton cargo = new JButton("Registrar cargo $25.000");
        JButton pago = new JButton("Registrar pago $25.000");
        JButton deshacer = new JButton("Deshacer última operación");
        EstiloSGA.estilizarBoton(cargo, false);
        EstiloSGA.estilizarBoton(pago, true);
        EstiloSGA.estilizarBoton(deshacer, false);
        cargo.addActionListener(evento -> operarFolio(0));
        pago.addActionListener(evento -> operarFolio(1));
        deshacer.addActionListener(evento -> operarFolio(2));
        acciones.add(new JLabel("Seleccione la reserva y luego gestione su folio."));
        acciones.add(cargo);
        acciones.add(pago);
        acciones.add(deshacer);
        panel.add(acciones, BorderLayout.NORTH);
        panel.add(panelCuenta, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return panel;
    }

    private JPanel crearPanelExterno() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel solicitud = new JPanel(new FlowLayout(FlowLayout.LEADING));
        JTextField plataforma = new JTextField("Canal externo", 12);
        JTextField documento = new JTextField("1002", 8);
        JTextField codigo = new JTextField("A101", 6);
        JTextField entrada = new JTextField(LocalDate.now().plusDays(8).toString(), 10);
        JTextField salida = new JTextField(LocalDate.now().plusDays(10).toString(), 10);
        JTextField personas = new JTextField("2", 3);
        for (JTextField campo : new JTextField[]{plataforma, documento, codigo, entrada, salida, personas}) {
            EstiloSGA.estilizarCampo(campo);
        }
        JButton encolar = new JButton("Encolar solicitud");
        JButton procesar = new JButton("Procesar siguiente (FIFO)");
        JButton turno = new JButton("Avanzar turno");
        EstiloSGA.estilizarBoton(encolar, false);
        EstiloSGA.estilizarBoton(procesar, true);
        EstiloSGA.estilizarBoton(turno, false);
        solicitud.add(new JLabel("Plataforma"));
        solicitud.add(plataforma);
        solicitud.add(new JLabel("Documento"));
        solicitud.add(documento);
        solicitud.add(new JLabel("Apartamento"));
        solicitud.add(codigo);
        solicitud.add(new JLabel("Entrada"));
        solicitud.add(entrada);
        solicitud.add(new JLabel("Salida"));
        solicitud.add(salida);
        solicitud.add(new JLabel("Personas"));
        solicitud.add(personas);
        solicitud.add(encolar);
        solicitud.add(procesar);
        solicitud.add(turno);
        encolar.addActionListener(evento -> encolarSolicitud(
                plataforma, documento, codigo, entrada, salida, personas));
        procesar.addActionListener(evento -> procesarSolicitud());
        turno.addActionListener(evento -> {
            sistema.avanzarTurno();
            refrescar();
        });

        areaExterno.setEditable(false);
        EstiloSGA.estilizarArea(areaExterno);
        etiquetaTurno.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.add(etiquetaTurno, BorderLayout.NORTH);
        cabecera.add(solicitud, BorderLayout.CENTER);
        panel.add(cabecera, BorderLayout.NORTH);
        panel.add(new JScrollPane(areaExterno), BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return panel;
    }

    private void agregarFavorito() {
        Huesped huesped = sistema.buscarHuesped(campoDocumento.getText().trim());
        Apartamento apartamento = sistema.buscarApartamento(campoCodigoFavorito.getText().trim());
        if (huesped == null || apartamento == null) {
            areaHuesped.setText("No se encontró el huésped o el apartamento.");
            return;
        }
        if (!huesped.agregarFavorito(apartamento.getCodigo())) {
            JOptionPane.showMessageDialog(this, "Ese apartamento ya está en favoritos.");
        }
        refrescarHuesped();
    }

    private void refrescarHuesped() {
        Huesped huesped = sistema.buscarHuesped(campoDocumento.getText().trim());
        if (huesped == null) {
            areaHuesped.setText("Huésped no encontrado.");
            return;
        }
        StringBuilder texto = new StringBuilder(huesped.getNombre())
                .append(" (").append(huesped.getDocumento()).append(")\nFavoritos:\n");
        for (String favorito : huesped.getFavoritos()) {
            texto.append(" - ").append(favorito).append('\n');
        }
        texto.append("Historial:\n");
        for (Object registro : huesped.getHistorial()) {
            texto.append(" - ").append(registro).append('\n');
        }
        areaHuesped.setText(texto.toString());
    }

    private void operarFolio(int operacion) {
        Reserva reserva = panelCuenta.getReservaSeleccionada();
        if (reserva == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una reserva con folio.");
            return;
        }
        try {
            if (operacion == 0) {
                reserva.getFolio().registrarCargo("Cargo de recepción", 25000);
            } else if (operacion == 1) {
                reserva.getFolio().registrarPago("Pago de recepción", 25000);
            } else {
                reserva.getFolio().deshacerUltimaOperacion();
            }
            panelCuenta.actualizar();
        } catch (RuntimeException error) {
            JOptionPane.showMessageDialog(this, error.getMessage());
        }
    }

    private void encolarSolicitud(JTextField plataforma, JTextField documento, JTextField codigo,
            JTextField entrada, JTextField salida, JTextField personas) {
        try {
            sistema.encolarExterna(new SolicitudExterna(plataforma.getText().trim(),
                    documento.getText().trim(), codigo.getText().trim(),
                    LocalDate.parse(entrada.getText().trim()), LocalDate.parse(salida.getText().trim()),
                    Integer.parseInt(personas.getText().trim())));
            refrescar();
        } catch (IllegalArgumentException error) {
            JOptionPane.showMessageDialog(this, "Solicitud inválida: " + error.getMessage());
        }
    }

    private void procesarSolicitud() {
        try {
            String resultado = sistema.procesarSiguienteExterna();
            refrescar();
            if (cambioReservasListener != null) {
                cambioReservasListener.reservasCambiaron();
            }
            JOptionPane.showMessageDialog(this, resultado);
        } catch (FechasInvalidasException error) {
            JOptionPane.showMessageDialog(this, error.getMessage());
        }
    }

    private void refrescar() {
        refrescarHuesped();
        panelCuenta.actualizar();
        etiquetaTurno.setText("Turno de recepción: " + sistema.turnoActual());
        StringBuilder cola = new StringBuilder("Solicitudes externas, en orden FIFO:\n");
        for (SolicitudExterna solicitud : sistema.getCanalExterno()) {
            cola.append(" - ").append(solicitud).append('\n');
        }
        if (sistema.getCanalExterno().estaVacia()) {
            cola.append(" (sin solicitudes pendientes)\n");
        }
        areaExterno.setText(cola.toString());
    }
}
