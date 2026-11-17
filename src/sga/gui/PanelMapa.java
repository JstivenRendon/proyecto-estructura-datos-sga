package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

import estructuras.Grafo;
import estructuras.ListaEnlazada;
import modelo.Apartamento;
import modelo.NodoEdificio;
import servicios.SistemaAlojamiento;

/**
 * Mapa interactivo del edificio. Dibuja el grafo (nodos y pasillos con su peso),
 * colorea los apartamentos según su estado, resalta la ruta de Dijkstra y permite
 * simular el cierre de un pasillo con un clic para ver qué apartamentos
 * quedan inalcanzables desde la recepción.
 */
public class PanelMapa extends JPanel {
    private static final int RADIO = 26;
    private static final int ALTO_BARRA = 70;
    private static final double TOLERANCIA_CLIC = 8;

    private SistemaAlojamiento sistema;
    private ListaEnlazada<?> ruta = new ListaEnlazada<>();
    private NodoEdificio cierreA;
    private NodoEdificio cierreB;
    private final ListaEnlazada<NodoEdificio> inalcanzables = new ListaEnlazada<>();

    public PanelMapa(SistemaAlojamiento sistema) {
        this.sistema = sistema;
        setBackground(Color.WHITE);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                alternarPasilloEn(e.getX(), e.getY());
            }
        });
    }

    /** Cambia el sistema mostrado (por ejemplo, al cargar otro escenario) y limpia ruta y cierres. */
    public void setSistema(SistemaAlojamiento sistema) {
        this.sistema = sistema;
        this.ruta = new ListaEnlazada<>();
        abrirPasillo();
    }

    /** Define la ruta (lista de nodos) que se resalta en azul. */
    public void setRuta(ListaEnlazada<?> ruta) {
        this.ruta = ruta == null ? new ListaEnlazada<>() : ruta;
        repaint();
    }

    /** Cierra el pasillo entre dos nodos y calcula qué apartamentos quedan aislados. */
    public void cerrarPasillo(NodoEdificio a, NodoEdificio b) {
        cierreA = a;
        cierreB = b;
        inalcanzables.limpiar();
        boolean hayRecepcion = false;
        for (NodoEdificio nodo : sistema.getNodosMapa()) {
            if (nodo.getTipo() == NodoEdificio.Tipo.RECEPCION) {
                hayRecepcion = true;
            }
        }
        if (hayRecepcion) {
            for (NodoEdificio nodo : sistema.getNodosMapa()) {
                if (nodo.getTipo() == NodoEdificio.Tipo.APARTAMENTO
                        && sistema.pasilloCierraAisla(a.getId(), b.getId(), nodo.getId())) {
                    inalcanzables.agregarFinal(nodo);
                }
            }
        }
        repaint();
    }

    /** Vuelve a abrir el pasillo cerrado. */
    public void abrirPasillo() {
        cierreA = null;
        cierreB = null;
        inalcanzables.limpiar();
        repaint();
    }

    public boolean hayPasilloCerrado() {
        return cierreA != null;
    }

    // ------------------------------------------------------------------ dibujo

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        NodoEdificio[] nodos = nodosComoArreglo();
        if (nodos.length == 0) {
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("No hay mapa cargado. Usa \"Cargar escenario de prueba\" en Inicio.", 30, 40);
            g2.dispose();
            return;
        }
        double[][] pos = posiciones(nodos);
        Grafo<NodoEdificio> mapa = sistema.getMapa();

        // Pasillos
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        for (int i = 0; i < nodos.length; i++) {
            for (Grafo.Arista<NodoEdificio> arista : mapa.adyacentes(nodos[i])) {
                int j = indiceDe(nodos, arista.getDestino());
                if (j <= i) {
                    continue;
                }
                dibujarPasillo(g2, nodos[i], nodos[j], pos[0][i], pos[1][i], pos[0][j], pos[1][j], arista.getPeso());
            }
        }

        // Nodos
        for (int i = 0; i < nodos.length; i++) {
            dibujarNodo(g2, nodos[i], pos[0][i], pos[1][i]);
        }

        dibujarLeyenda(g2);
        dibujarEstado(g2);
        g2.dispose();
    }

    private void dibujarPasillo(Graphics2D g2, NodoEdificio a, NodoEdificio b,
                                double x1, double y1, double x2, double y2, double peso) {
        boolean cerrado = esPasilloCerrado(a, b);
        boolean enRuta = estaEnRuta(a, b);
        if (cerrado) {
            g2.setColor(new Color(211, 47, 47));
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
                    new float[]{8f, 6f}, 0f));
        } else if (enRuta) {
            g2.setColor(new Color(25, 118, 210));
            g2.setStroke(new BasicStroke(6f));
        } else {
            g2.setColor(new Color(150, 150, 150));
            g2.setStroke(new BasicStroke(2f));
        }
        g2.draw(new Line2D.Double(x1, y1, x2, y2));
        g2.setStroke(new BasicStroke(1f));

        String texto = formatoPeso(peso) + (cerrado ? " (cerrado)" : "");
        FontMetrics fm = g2.getFontMetrics();
        int mx = (int) ((x1 + x2) / 2);
        int my = (int) ((y1 + y2) / 2);
        int ancho = fm.stringWidth(texto) + 8;
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(mx - ancho / 2, my - 9, ancho, 18, 8, 8);
        g2.setColor(cerrado ? new Color(211, 47, 47) : (enRuta ? new Color(25, 118, 210) : Color.DARK_GRAY));
        g2.drawString(texto, mx - ancho / 2 + 4, my + 5);
    }

    private void dibujarNodo(Graphics2D g2, NodoEdificio nodo, double x, double y) {
        int cx = (int) x;
        int cy = (int) y;
        boolean aislado = contiene(inalcanzables, nodo);

        g2.setColor(colorDe(nodo));
        g2.fillOval(cx - RADIO, cy - RADIO, RADIO * 2, RADIO * 2);
        g2.setColor(aislado ? new Color(211, 47, 47) : Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(aislado ? 4f : 2f));
        g2.drawOval(cx - RADIO, cy - RADIO, RADIO * 2, RADIO * 2);
        g2.setStroke(new BasicStroke(1f));

        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Color.BLACK);
        g2.drawString(nodo.getId(), cx - fm.stringWidth(nodo.getId()) / 2, cy + 4);

        if (aislado) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            fm = g2.getFontMetrics();
            String aviso = "inalcanzable";
            g2.setColor(new Color(211, 47, 47));
            g2.drawString(aviso, cx - fm.stringWidth(aviso) / 2, cy + RADIO + 14);
        }
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }

    private void dibujarLeyenda(Graphics2D g2) {
        String[] nombres = {"Preparado", "Ocupado", "En preparación", "Fuera de servicio", "Recepción / servicio"};
        Color[] colores = {colorPreparado(), colorOcupado(), colorEnPreparacion(), colorFueraDeServicio(),
                new Color(224, 224, 224)};
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        int x = 12;
        int y = 14;
        for (int i = 0; i < nombres.length; i++) {
            g2.setColor(colores[i]);
            g2.fillOval(x, y - 10, 13, 13);
            g2.setColor(Color.DARK_GRAY);
            g2.drawOval(x, y - 10, 13, 13);
            g2.drawString(nombres[i], x + 18, y + 1);
            y += 18;
        }
        g2.setColor(new Color(25, 118, 210));
        g2.setStroke(new BasicStroke(5f));
        g2.drawLine(x, y - 4, x + 14, y - 4);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Ruta más corta (Dijkstra)", x + 18, y);
    }

    private void dibujarEstado(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2.setColor(Color.DARK_GRAY);
        int y = getHeight() - ALTO_BARRA + 22;
        g2.drawString("Haz clic sobre un pasillo para cerrarlo o abrirlo y ver qué apartamentos quedan aislados.", 14, y);
        if (!hayPasilloCerrado()) {
            return;
        }
        g2.setColor(new Color(211, 47, 47));
        g2.drawString("Pasillo cerrado: " + cierreA.getId() + " — " + cierreB.getId(), 14, y + 20);
        StringBuilder lista = new StringBuilder();
        for (NodoEdificio nodo : inalcanzables) {
            if (lista.length() > 0) {
                lista.append(", ");
            }
            lista.append(nodo.getId());
        }
        g2.drawString("Apartamentos inalcanzables desde recepción: "
                + (lista.length() == 0 ? "ninguno (hay una ruta alterna)" : lista), 14, y + 40);
    }

    // ------------------------------------------------------------- interacción

    private void alternarPasilloEn(int clicX, int clicY) {
        NodoEdificio[] nodos = nodosComoArreglo();
        if (nodos.length == 0) {
            return;
        }
        double[][] pos = posiciones(nodos);
        Grafo<NodoEdificio> mapa = sistema.getMapa();
        for (int i = 0; i < nodos.length; i++) {
            for (Grafo.Arista<NodoEdificio> arista : mapa.adyacentes(nodos[i])) {
                int j = indiceDe(nodos, arista.getDestino());
                if (j <= i) {
                    continue;
                }
                double distancia = Line2D.ptSegDist(pos[0][i], pos[1][i], pos[0][j], pos[1][j], clicX, clicY);
                if (distancia <= TOLERANCIA_CLIC) {
                    if (esPasilloCerrado(nodos[i], nodos[j])) {
                        abrirPasillo();
                    } else {
                        cerrarPasillo(nodos[i], nodos[j]);
                    }
                    return;
                }
            }
        }
    }

    // ----------------------------------------------------------------- apoyo

    private NodoEdificio[] nodosComoArreglo() {
        ListaEnlazada<NodoEdificio> lista = sistema.getNodosMapa();
        NodoEdificio[] arreglo = new NodoEdificio[lista.tamano()];
        int i = 0;
        for (NodoEdificio nodo : lista) {
            arreglo[i++] = nodo;
        }
        return arreglo;
    }

    /**
     * Distribución en columnas: la recepción queda a la izquierda y cada columna
     * agrupa los nodos que están a los mismos pasillos de distancia (recorrido BFS).
     * Los hijos de un nodo quedan juntos en vertical, así las líneas casi no se cruzan.
     */
    private double[][] posiciones(NodoEdificio[] nodos) {
        int n = nodos.length;
        double[] xs = new double[n];
        double[] ys = new double[n];

        int raiz = 0;
        for (int i = 0; i < n; i++) {
            if (nodos[i].getTipo() == NodoEdificio.Tipo.RECEPCION) {
                raiz = i;
                break;
            }
        }

        // BFS desde la raíz: nivel (columna) y padre de cada nodo
        int[] padre = new int[n];
        int[] nivel = new int[n];
        for (int i = 0; i < n; i++) {
            padre[i] = -1;
            nivel[i] = -1;
        }
        int[] cola = new int[n];
        int ini = 0;
        int fin = 0;
        cola[fin++] = raiz;
        nivel[raiz] = 0;
        Grafo<NodoEdificio> mapa = sistema.getMapa();
        while (ini < fin) {
            int u = cola[ini++];
            for (Grafo.Arista<NodoEdificio> arista : mapa.adyacentes(nodos[u])) {
                int v = indiceDe(nodos, arista.getDestino());
                if (v >= 0 && nivel[v] < 0) {
                    nivel[v] = nivel[u] + 1;
                    padre[v] = u;
                    cola[fin++] = v;
                }
            }
        }
        int maxNivel = 0;
        for (int i = 0; i < n; i++) {
            maxNivel = Math.max(maxNivel, nivel[i]);
        }

        // Hojas de cada subárbol (los hijos se procesan antes que su padre)
        int[] hojas = new int[n];
        for (int k = fin - 1; k >= 0; k--) {
            int u = cola[k];
            if (hojas[u] == 0) {
                hojas[u] = 1;
            }
            if (padre[u] >= 0) {
                hojas[padre[u]] += hojas[u];
            }
        }

        // Cada nodo recibe una franja vertical proporcional a sus hojas
        double areaAlto = getHeight() - ALTO_BARRA;
        double arriba = 55;
        double abajo = Math.max(arriba + 60, areaAlto - 40);
        double margenX = 70;
        double paso = (getWidth() - 2 * margenX) / Math.max(1, maxNivel);
        double[] franjaIni = new double[n];
        double[] franjaFin = new double[n];
        franjaIni[raiz] = arriba;
        franjaFin[raiz] = abajo;
        for (int k = 0; k < fin; k++) {
            int u = cola[k];
            double cursor = franjaIni[u];
            double alto = franjaFin[u] - franjaIni[u];
            for (int m = k + 1; m < fin; m++) {
                int v = cola[m];
                if (padre[v] == u) {
                    franjaIni[v] = cursor;
                    franjaFin[v] = cursor + alto * hojas[v] / hojas[u];
                    cursor = franjaFin[v];
                }
            }
            xs[u] = margenX + nivel[u] * paso;
            ys[u] = (franjaIni[u] + franjaFin[u]) / 2;
        }

        // Nodos sin conexión con la recepción: fila inferior
        int sueltos = 0;
        for (int i = 0; i < n; i++) {
            if (nivel[i] < 0) {
                sueltos++;
            }
        }
        int colocados = 0;
        for (int i = 0; i < n; i++) {
            if (nivel[i] < 0) {
                colocados++;
                xs[i] = getWidth() * colocados / (sueltos + 1.0);
                ys[i] = areaAlto - 20;
            }
        }
        return new double[][]{xs, ys};
    }

    private int indiceDe(NodoEdificio[] nodos, NodoEdificio buscado) {
        for (int i = 0; i < nodos.length; i++) {
            if (nodos[i].equals(buscado)) {
                return i;
            }
        }
        return -1;
    }

    private boolean esPasilloCerrado(NodoEdificio a, NodoEdificio b) {
        return cierreA != null
                && ((a.equals(cierreA) && b.equals(cierreB)) || (a.equals(cierreB) && b.equals(cierreA)));
    }

    private boolean estaEnRuta(NodoEdificio a, NodoEdificio b) {
        Object anterior = null;
        for (Object actual : ruta) {
            if (anterior != null
                    && ((anterior.equals(a) && actual.equals(b)) || (anterior.equals(b) && actual.equals(a)))) {
                return true;
            }
            anterior = actual;
        }
        return false;
    }

    private boolean contiene(ListaEnlazada<NodoEdificio> lista, NodoEdificio buscado) {
        for (NodoEdificio nodo : lista) {
            if (nodo.equals(buscado)) {
                return true;
            }
        }
        return false;
    }

    private String formatoPeso(double peso) {
        return peso == Math.rint(peso) ? ((long) peso) + " m" : String.format("%.1f m", peso);
    }

    private Color colorDe(NodoEdificio nodo) {
        Apartamento apto = nodo.getApartamento();
        if (nodo.getTipo() != NodoEdificio.Tipo.APARTAMENTO || apto == null) {
            return new Color(224, 224, 224);
        }
        switch (apto.getEstado()) {
            case PREPARADO:
                return colorPreparado();
            case OCUPADO:
                return colorOcupado();
            case PENDIENTE_PREPARACION:
            case EN_PREPARACION:
                return colorEnPreparacion();
            default:
                return colorFueraDeServicio();
        }
    }

    private Color colorPreparado() {
        return new Color(165, 214, 167);
    }

    private Color colorOcupado() {
        return new Color(144, 202, 249);
    }

    private Color colorEnPreparacion() {
        return new Color(255, 241, 118);
    }

    private Color colorFueraDeServicio() {
        return new Color(239, 154, 154);
    }
}