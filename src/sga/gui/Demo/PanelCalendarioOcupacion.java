package gui.Demo;

import estructuras.ArbolBinarioBusqueda;
import modelo.Apartamento;
import modelo.EstadoOperativo;
import modelo.EstadoReserva;
import modelo.Huesped;
import modelo.Reserva;
import servicios.GestorReservas;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;

public class PanelCalendarioOcupacion extends JPanel implements CambioReservasListener {
    private static final String[] MESES = {
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };
    private static final Color COLOR_LIBRE = new Color(218, 239, 218);
    private static final Color COLOR_OCUPADO = new Color(143, 190, 235);
    private static final Color COLOR_PENDIENTE = new Color(255, 218, 126);
    private static final Color COLOR_BLOQUEADO = new Color(239, 151, 151);

    private final GestorReservas gestorReservas;
    private final JComboBox<String> selectorMes = new JComboBox<>(MESES);
    private final JSpinner selectorAnio = new JSpinner(
            new SpinnerNumberModel(LocalDate.now().getYear(), 1900, 9999, 1));
    private final JLabel inspeccion = new JLabel("Seleccione una fecha para consultar su ocupación.");
    private final JTable tabla = new JTable();
    private final JTable columnaFija = new JTable();
    private Apartamento[] apartamentos = new Apartamento[0];
    private YearMonth mesMostrado = YearMonth.now();
    private int filaInspeccionada = -1;
    private int diaInspeccionado = -1;

    public PanelCalendarioOcupacion(GestorReservas gestorReservas) {
        if (gestorReservas == null) {
            throw new IllegalArgumentException("El gestor de reservas no puede ser null");
        }
        this.gestorReservas = gestorReservas;
        construirInterfaz();
        selectorMes.setSelectedIndex(mesMostrado.getMonthValue() - 1);
        refrescarCalendario();
    }

    @Override
    public void reservasCambiaron() {
        refrescarCalendario();
    }

    public void refrescarCalendario() {
        mesMostrado = YearMonth.of((Integer) selectorAnio.getValue(),
                selectorMes.getSelectedIndex() + 1);
        cargarFilasYColumnas();
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(8, 8));
        JPanel controles = new JPanel(new BorderLayout(6, 6));
        JPanel navegacion = new JPanel(new FlowLayout(FlowLayout.LEADING));
        JButton anterior = new JButton("< Mes Anterior");
        anterior.addActionListener(evento -> cambiarMes(-1));
        JButton siguiente = new JButton("Mes Siguiente >");
        siguiente.addActionListener(evento -> cambiarMes(1));
        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(evento -> refrescarCalendario());
        selectorMes.addActionListener(evento -> refrescarCalendario());
        selectorAnio.addChangeListener(evento -> refrescarCalendario());

        navegacion.add(anterior);
        navegacion.add(selectorMes);
        navegacion.add(new JLabel("Año:"));
        navegacion.add(selectorAnio);
        navegacion.add(siguiente);
        navegacion.add(actualizar);
        controles.add(navegacion, BorderLayout.NORTH);
        controles.add(crearLeyenda(), BorderLayout.SOUTH);
        add(controles, BorderLayout.NORTH);

        JScrollPane desplazamiento = new JScrollPane(tabla);
        desplazamiento.setRowHeaderView(columnaFija);
        desplazamiento.setCorner(JScrollPane.UPPER_LEFT_CORNER, columnaFija.getTableHeader());
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        columnaFija.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.setRowSelectionAllowed(true);
        tabla.setColumnSelectionAllowed(true);
        tabla.setCellSelectionEnabled(true);
        columnaFija.setSelectionModel(tabla.getSelectionModel());
        columnaFija.setRowHeight(tabla.getRowHeight());
        columnaFija.setFocusable(false);
        columnaFija.setRowSelectionAllowed(false);
        tabla.setDefaultRenderer(Object.class, new OcupacionRenderer());
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                inspeccionarCelda(evento);
            }
        });
        columnaFija.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                int fila = columnaFija.rowAtPoint(evento.getPoint());
                if (fila >= 0 && mesMostrado.lengthOfMonth() > 0) {
                    inspeccionar(fila, 1);
                }
            }
        });
        add(desplazamiento, BorderLayout.CENTER);

        inspeccion.setBorder(BorderFactory.createTitledBorder("Detalle de ocupación"));
        inspeccion.setVerticalAlignment(SwingConstants.TOP);
        add(inspeccion, BorderLayout.SOUTH);
    }

    private JPanel crearLeyenda() {
        JPanel leyenda = new JPanel(new GridLayout(1, 4, 8, 0));
        leyenda.add(elementoLeyenda(COLOR_LIBRE, "Libre"));
        leyenda.add(elementoLeyenda(COLOR_OCUPADO, "Ocupado / Confirmado"));
        leyenda.add(elementoLeyenda(COLOR_PENDIENTE, "Pendiente de pago"));
        leyenda.add(elementoLeyenda(COLOR_BLOQUEADO, "Bloqueado / Fuera de servicio"));
        return leyenda;
    }

    private JLabel elementoLeyenda(Color color, String texto) {
        JLabel etiqueta = new JLabel("  " + texto);
        etiqueta.setOpaque(true);
        etiqueta.setBackground(color);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        return etiqueta;
    }

    private void cambiarMes(int desplazamiento) {
        YearMonth nuevoMes = mesMostrado.plusMonths(desplazamiento);
        selectorMes.setSelectedIndex(nuevoMes.getMonthValue() - 1);
        selectorAnio.setValue(nuevoMes.getYear());
        refrescarCalendario();
    }

    private void cargarFilasYColumnas() {
        ArbolBinarioBusqueda<Apartamento> catalogo = gestorReservas.getCatalogoApartamentos();
        apartamentos = new Apartamento[catalogo.contarElementos()];
        int fila = 0;
        for (Apartamento apartamento : catalogo) {
            apartamentos[fila++] = apartamento;
        }

        int dias = mesMostrado.lengthOfMonth();
        Object[][] datos = new Object[apartamentos.length][dias + 1];
        String[] columnas = new String[dias + 1];
        columnas[0] = "Apartamento";
        for (int dia = 1; dia <= dias; dia++) {
            columnas[dia] = Integer.toString(dia);
        }
        for (int indiceApartamento = 0; indiceApartamento < apartamentos.length; indiceApartamento++) {
            Apartamento apartamento = apartamentos[indiceApartamento];
            datos[indiceApartamento][0] = apartamento.getCodigo() + " - " + apartamento.getNombre();
            for (int dia = 1; dia <= dias; dia++) {
                LocalDate fecha = mesMostrado.atDay(dia);
                datos[indiceApartamento][dia] = consultarOcupacion(apartamento, fecha);
            }
        }

        DefaultTableModel modelo = new DefaultTableModel(datos, columnas) {
            @Override
            public boolean isCellEditable(int filaModelo, int columnaModelo) {
                return false;
            }
        };
        tabla.setModel(modelo);
        columnaFija.setModel(modelo);
        eliminarColumnasExceptoApartamento(columnaFija);
        tabla.removeColumn(tabla.getColumnModel().getColumn(0));
        TableColumn primeraColumna = columnaFija.getColumnModel().getColumn(0);
        primeraColumna.setPreferredWidth(210);
        primeraColumna.setMinWidth(210);
        primeraColumna.setMaxWidth(210);
        for (int dia = 1; dia <= dias; dia++) {
            TableColumn columna = tabla.getColumnModel().getColumn(dia - 1);
            columna.setPreferredWidth(88);
            columna.setMinWidth(70);
        }
        setToolTips(tabla);
        setToolTips(columnaFija);
        revalidate();
        repaint();
        if (filaInspeccionada >= 0 && filaInspeccionada < apartamentos.length
                && diaInspeccionado >= 1 && diaInspeccionado <= dias) {
            inspeccionar(filaInspeccionada, diaInspeccionado);
        } else {
            filaInspeccionada = -1;
            diaInspeccionado = -1;
            inspeccion.setText("Seleccione una fecha para consultar su ocupación.");
        }
    }

    private void eliminarColumnasExceptoApartamento(JTable tablaFija) {
        while (tablaFija.getColumnModel().getColumnCount() > 1) {
            tablaFija.removeColumn(tablaFija.getColumnModel().getColumn(1));
        }
    }

    private void setToolTips(JTable tablaConTooltips) {
        tablaConTooltips.setToolTipText("");
    }

    private CeldaOcupacion consultarOcupacion(Apartamento apartamento, LocalDate fecha) {
        if (apartamento.getEstadoOperativo() == EstadoOperativo.FUERA_DE_SERVICIO) {
            return new CeldaOcupacion(EstadoCelda.BLOQUEADO, null,
                    apartamento.getMotivoFueraDeServicio());
        }
        for (Reserva reserva : apartamento.getReservas()) {
            EstadoReserva estado = reserva.getEstado();
            if (estado == EstadoReserva.CANCELADA || estado == EstadoReserva.NO_SHOW
                    || estado == EstadoReserva.FINALIZADA) {
                continue;
            }
            if (!fecha.isBefore(reserva.getEstancia().getFechaEntrada())
                    && fecha.isBefore(reserva.getEstancia().getFechaSalida())) {
                if (estado == EstadoReserva.PENDIENTE) {
                    return new CeldaOcupacion(EstadoCelda.PENDIENTE, reserva, null);
                }
                if (estado == EstadoReserva.CONFIRMADA || estado == EstadoReserva.EN_CURSO) {
                    return new CeldaOcupacion(EstadoCelda.OCUPADO, reserva, null);
                }
            }
        }
        return new CeldaOcupacion(EstadoCelda.LIBRE, null, null);
    }

    private void inspeccionarCelda(MouseEvent evento) {
        int fila = tabla.rowAtPoint(evento.getPoint());
        int columnaVista = tabla.columnAtPoint(evento.getPoint());
        if (fila < 0 || columnaVista < 0) {
            return;
        }
        int columnaModelo = tabla.convertColumnIndexToModel(columnaVista);
        if (columnaModelo >= 1) {
            inspeccionar(fila, columnaModelo);
        }
    }

    private void inspeccionar(int fila, int dia) {
        if (fila < 0 || fila >= apartamentos.length || dia < 1 || dia > mesMostrado.lengthOfMonth()) {
            return;
        }
        filaInspeccionada = fila;
        diaInspeccionado = dia;
        Apartamento apartamento = apartamentos[fila];
        LocalDate fecha = mesMostrado.atDay(dia);
        CeldaOcupacion celda = consultarOcupacion(apartamento, fecha);
        StringBuilder detalle = new StringBuilder("<html><b>")
                .append(apartamento.getCodigo()).append(" - ").append(apartamento.getNombre())
                .append("</b><br>Fecha: ").append(fecha)
                .append("<br>Estado: ").append(celda.estado.texto);
        if (celda.reserva != null) {
            Reserva reserva = celda.reserva;
            Huesped titular = reserva.getTitular();
            detalle.append("<br>Reserva: ").append(reserva.getIdReserva())
                    .append("<br>Huésped: ").append(titular.getNombre())
                    .append(" (").append(titular.getDocumento()).append(')')
                    .append("<br>Canal: ").append(reserva.getCanal())
                    .append("<br>Estancia: ").append(reserva.getEstancia().getFechaEntrada())
                    .append(" a ").append(reserva.getEstancia().getFechaSalida());
        } else if (celda.estado == EstadoCelda.BLOQUEADO) {
            detalle.append("<br>Motivo: ").append(celda.motivo == null ? "No especificado" : celda.motivo);
        }
        inspeccion.setText(detalle.append("</html>").toString());
    }

    private enum EstadoCelda {
        LIBRE("Libre"),
        OCUPADO("Ocupado / confirmado"),
        PENDIENTE("Pendiente de pago"),
        BLOQUEADO("Bloqueado / fuera de servicio");

        private final String texto;

        EstadoCelda(String texto) {
            this.texto = texto;
        }
    }

    private static final class CeldaOcupacion {
        private final EstadoCelda estado;
        private final Reserva reserva;
        private final String motivo;

        private CeldaOcupacion(EstadoCelda estado, Reserva reserva, String motivo) {
            this.estado = estado;
            this.reserva = reserva;
            this.motivo = motivo;
        }

        private Color color() {
            switch (estado) {
                case OCUPADO:
                    return COLOR_OCUPADO;
                case PENDIENTE:
                    return COLOR_PENDIENTE;
                case BLOQUEADO:
                    return COLOR_BLOQUEADO;
                default:
                    return COLOR_LIBRE;
            }
        }

        private String etiqueta() {
            if (reserva != null) {
                String id = reserva.getIdReserva();
                return id == null ? estado.texto : id;
            }
            return estado == EstadoCelda.BLOQUEADO ? "Bloqueado" : "Libre";
        }

        private String tooltip() {
            if (estado == EstadoCelda.BLOQUEADO) {
                return motivo == null || motivo.trim().isEmpty()
                        ? "Fuera de servicio" : "Fuera de servicio: " + motivo;
            }
            if (reserva != null) {
                return reserva.getIdReserva() + " - " + estado.texto;
            }
            return "Libre";
        }
    }

    private static final class OcupacionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor,
                boolean seleccionado, boolean tieneFoco, int fila, int columna) {
            super.getTableCellRendererComponent(tabla, valor, seleccionado, tieneFoco, fila, columna);
            if (valor instanceof CeldaOcupacion) {
                CeldaOcupacion celda = (CeldaOcupacion) valor;
                setText(celda.etiqueta());
                setToolTipText(celda.tooltip());
                setBackground(celda.color());
                setForeground(Color.DARK_GRAY);
                setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                setToolTipText(null);
                setBackground(Color.WHITE);
                setForeground(Color.DARK_GRAY);
                setHorizontalAlignment(SwingConstants.LEADING);
            }
            return this;
        }
    }
}
