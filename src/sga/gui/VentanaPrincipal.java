package gui;

import estructuras.Pila;
import modelo.Folio;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;

/**
 * Ventana principal del SGA. Cambia entre pantallas y recuerda
 * las visitadas con una pila para poder volver atrás.
 */
public class VentanaPrincipal extends JFrame {

    private static final String[] PANTALLAS = {
            "Inicio", "Reservas", "Calendario", "Mi cuenta", "Estadísticas"
    };

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);
    private final Pila<String> historial = new Pila<>();
    private String pantallaActual;

    public VentanaPrincipal() {
        setTitle("SGA - Aparta-Suites UQ");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(crearMenu(), BorderLayout.NORTH);
        add(contenido, BorderLayout.CENTER);

        for (String nombre : PANTALLAS) {
            if (!nombre.equals("Mi cuenta")) {
                agregarPantalla(nombre);
            }
        }
        contenido.add(new PanelCuenta(crearFolioDeEjemplo()), "Mi cuenta");

        pantallaActual = PANTALLAS[0];
        cartas.show(contenido, pantallaActual);
    }

    private JPanel crearMenu() {
        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton atras = new JButton("← Atrás");
        atras.addActionListener(e -> volver());
        menu.add(atras);

        for (String nombre : PANTALLAS) {
            JButton boton = new JButton(nombre);
            boton.addActionListener(e -> mostrar(nombre));
            menu.add(boton);
        }
        return menu;
    }

    private void agregarPantalla(String nombre) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Pantalla: " + nombre, SwingConstants.CENTER));
        contenido.add(panel, nombre);
    }

    private Folio crearFolioDeEjemplo() {
        Folio folio = new Folio();
        folio.registrarCargo("Estancia 3 noches", 450000);
        folio.registrarCargo("Desayuno", 30000);
        folio.registrarPago("Anticipo 30%", 150000);
        return folio;
    }

    private void mostrar(String nombre) {
        if (nombre.equals(pantallaActual)) {
            return;
        }
        historial.apilar(pantallaActual);
        pantallaActual = nombre;
        cartas.show(contenido, nombre);
    }

    private void volver() {
        if (historial.estaVacia()) {
            return;
        }
        pantallaActual = historial.desapilar();
        cartas.show(contenido, pantallaActual);
    }
}