package gui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import estructuras.ListaEnlazada;
import servicios.SistemaAlojamiento;

public class PanelMapa extends JPanel {
    private final SistemaAlojamiento sistema;
    private ListaEnlazada<Object> ruta = new ListaEnlazada<>();

    public PanelMapa(SistemaAlojamiento sistema) {
        this.sistema = sistema;
        setBackground(Color.WHITE);
    }

    public void setRuta(ListaEnlazada<?> ruta) {
        this.ruta = (ListaEnlazada<Object>) ruta;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(220, 230, 240));
        g.fillRect(20, 20, getWidth() - 40, getHeight() - 40);
        g.setColor(Color.DARK_GRAY);
        g.drawString("Mapa de preparación", 30, 30);
        if (ruta != null && !ruta.estaVacia()) {
            g.setColor(new Color(68, 137, 255));
            g.drawString("Ruta activa", 30, getHeight() - 25);
        }
    }
}
