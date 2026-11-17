package gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

public final class EstiloSGA {
    public static final Color AZUL_NOCHE = new Color(25, 48, 69);
    public static final Color VERDE_UQ = new Color(25, 119, 108);
    public static final Color FONDO = new Color(241, 245, 248);
    public static final Color SUPERFICIE = Color.WHITE;
    public static final Color TEXTO = new Color(38, 55, 70);
    public static final Color TEXTO_SECUNDARIO = new Color(102, 119, 133);
    public static final Color BORDE = new Color(216, 225, 232);

    private EstiloSGA() {
    }

    public static void aplicarTema() {
        UIManager.put("Panel.background", FONDO);
        UIManager.put("OptionPane.background", SUPERFICIE);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("Button.background", SUPERFICIE);
        UIManager.put("Button.foreground", TEXTO);
        UIManager.put("Button.font", new Font("SansSerif", Font.BOLD, 13));
        UIManager.put("TabbedPane.background", FONDO);
        UIManager.put("TabbedPane.foreground", TEXTO);
        UIManager.put("TabbedPane.selected", SUPERFICIE);
        UIManager.put("TableHeader.background", AZUL_NOCHE);
        UIManager.put("TableHeader.foreground", SUPERFICIE);
        UIManager.put("TableHeader.font", new Font("SansSerif", Font.BOLD, 12));
        UIManager.put("ToolTip.background", AZUL_NOCHE);
        UIManager.put("ToolTip.foreground", SUPERFICIE);
    }

    public static JPanel crearCabecera(String titulo, String subtitulo) {
        JPanel cabecera = new JPanel(new BorderLayout(4, 3));
        cabecera.setBackground(AZUL_NOCHE);
        cabecera.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel marca = new JLabel("APARTA-SUITES UQ  /  SGA");
        marca.setForeground(new Color(154, 218, 204));
        marca.setFont(new Font("SansSerif", Font.BOLD, 11));
        JLabel encabezado = new JLabel(titulo);
        encabezado.setForeground(SUPERFICIE);
        encabezado.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel descripcion = new JLabel(subtitulo);
        descripcion.setForeground(new Color(207, 220, 230));
        descripcion.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel textos = new JPanel(new BorderLayout(0, 4));
        textos.setOpaque(false);
        JPanel tituloYMarca = new JPanel(new BorderLayout(0, 5));
        tituloYMarca.setOpaque(false);
        tituloYMarca.add(marca, BorderLayout.NORTH);
        tituloYMarca.add(encabezado, BorderLayout.CENTER);
        textos.add(tituloYMarca, BorderLayout.NORTH);
        textos.add(descripcion, BorderLayout.CENTER);
        cabecera.add(textos, BorderLayout.CENTER);
        return cabecera;
    }

    public static JPanel crearTarjeta(java.awt.LayoutManager layout) {
        JPanel tarjeta = new JPanel(layout);
        tarjeta.setBackground(SUPERFICIE);
        tarjeta.setBorder(new CompoundBorder(
                new LineBorder(BORDE, 1, true),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        return tarjeta;
    }

    public static void estilizarBoton(JButton boton, boolean principal) {
        boton.setFont(new Font("SansSerif", Font.BOLD, 13));
        boton.setForeground(principal ? SUPERFICIE : TEXTO);
        boton.setBackground(principal ? VERDE_UQ : SUPERFICIE);
        boton.setOpaque(true);
        boton.setFocusPainted(false);
        boton.setBorder(new CompoundBorder(
                new LineBorder(principal ? VERDE_UQ : BORDE, 1, true),
                BorderFactory.createEmptyBorder(8, 13, 8, 13)));
        boton.setMargin(new Insets(4, 8, 4, 8));
        boton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    public static void estilizarCampo(JTextField campo) {
        campo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campo.setForeground(TEXTO);
        campo.setBackground(SUPERFICIE);
        campo.setBorder(new CompoundBorder(
                new LineBorder(BORDE, 1, true),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
    }

    public static void estilizarArea(JTextArea area) {
        area.setFont(new Font("SansSerif", Font.PLAIN, 13));
        area.setForeground(TEXTO);
        area.setBackground(SUPERFICIE);
        area.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        area.setCaretColor(VERDE_UQ);
    }
}
