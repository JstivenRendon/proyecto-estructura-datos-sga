package pruebas;

import excepciones.FechasInvalidasException;
import modelo.Apartamento;
import modelo.CanalReserva;
import modelo.EstadoOperativo;
import modelo.EstadoReserva;
import modelo.Estancia;
import modelo.Huesped;
import modelo.Reserva;
import comparadores.ComparadorApartamentoPorCapacidad;
import comparadores.ComparadorApartamentoPorPrecio;
import comparadores.ComparadorApartamentoPorReservas;
import comparadores.ComparadorPrioridadPreparacion;
import estructuras.ArbolBinarioBusqueda;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;

public final class MainTestFase1 {
    private MainTestFase1() {
    }

    public static void main(String[] args) throws FechasInvalidasException {
        probarArbol();
        probarEstancia();
        probarComparadores();
        System.out.println("Pruebas de Fase 1 completadas correctamente.");
    }

    private static void probarArbol() {
        ArbolBinarioBusqueda<Apartamento> arbol = new ArbolBinarioBusqueda<>();
        verificar(arbol.estaVacio(), "árbol vacío inicial");
        verificar(arbol.calcularAltura() == -1, "altura de árbol vacío");
        arbol.insertar(apartamento("D", 4, 100, null));
        arbol.insertar(apartamento("B", 2, 100, null));
        arbol.insertar(apartamento("F", 6, 100, null));
        arbol.insertar(apartamento("A", 1, 100, null));
        arbol.insertar(apartamento("C", 3, 100, null));
        arbol.insertar(apartamento("E", 5, 100, null));
        arbol.insertar(apartamento("G", 7, 100, null));

        verificar(!arbol.estaVacio(), "árbol con elementos");
        verificar(arbol.contarElementos() == 7, "conteo inicial");
        verificar(arbol.calcularAltura() == 2, "altura inicial");
        verificar("C".equals(arbol.buscar(apartamento("C", 0, 0, null)).getCodigo()),
                "búsqueda");

        StringBuilder orden = new StringBuilder();
        for (Apartamento item : arbol) {
            orden.append(item.getCodigo());
        }
        verificar("ABCDEFG".contentEquals(orden), "iterador inorden");

        int[] recorridos = {0};
        arbol.recorridoInorden(item -> recorridos[0]++);
        arbol.recorridoPreorden(item -> recorridos[0]++);
        arbol.recorridoPostorden(item -> recorridos[0]++);
        verificar(recorridos[0] == 21, "recorridos recursivos");

        verificar(arbol.eliminar(apartamento("A", 0, 0, null)), "eliminar hoja");
        verificar(arbol.eliminar(apartamento("B", 0, 0, null)), "eliminar nodo con un hijo");
        verificar(arbol.eliminar(apartamento("D", 0, 0, null)), "eliminar nodo con dos hijos");
        verificar(arbol.contarElementos() == 4, "conteo luego de eliminar");
        verificar(arbol.buscar(apartamento("D", 0, 0, null)) == null, "búsqueda ausente");
    }

    private static void probarEstancia() throws FechasInvalidasException {
        Estancia estancia = new Estancia(
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 12));
        int noches = 0;
        for (LocalDate noche : estancia) {
            verificar(!noche.equals(estancia.getFechaSalida()), "excluir fecha de salida");
            noches++;
        }
        verificar(noches == 2, "número de noches");

        boolean fechasInvalidasRechazadas = false;
        try {
            new Estancia(estancia.getFechaEntrada(), estancia.getFechaEntrada());
        } catch (FechasInvalidasException esperada) {
            fechasInvalidasRechazadas = true;
        }
        verificar(fechasInvalidasRechazadas, "validación de fechas");
    }

    private static void probarComparadores() throws FechasInvalidasException {
        Apartamento[] apartamentos = {
            apartamento("A", 2, 200, null),
            apartamento("B", 5, 100, LocalTime.of(12, 0)),
            apartamento("C", 4, 150, LocalTime.of(9, 0))
        };

        ordenar(apartamentos, new ComparadorApartamentoPorPrecio());
        verificar("B".equals(apartamentos[0].getCodigo()), "comparador por precio");

        ordenar(apartamentos, new ComparadorApartamentoPorCapacidad());
        verificar("B".equals(apartamentos[0].getCodigo()), "comparador por capacidad");

        Estancia estancia = new Estancia(
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 12));
        Huesped huesped = new Huesped("Ana", "123", null, null, null, null);
        Reserva reserva = new Reserva("R1", huesped, apartamentos[0], estancia,
                CanalReserva.DIRECTA, EstadoReserva.CONFIRMADA,
                LocalDateTime.of(2026, 1, 1, 10, 0), 2);
        apartamentos[0].getReservas().insertar(reserva);
        ordenar(apartamentos, new ComparadorApartamentoPorReservas());
        verificar("C".equals(apartamentos[0].getCodigo()),
                "comparador por cantidad de reservas");

        ordenar(apartamentos, new ComparadorPrioridadPreparacion());
        verificar("C".equals(apartamentos[0].getCodigo())
                        && "B".equals(apartamentos[1].getCodigo())
                        && "A".equals(apartamentos[2].getCodigo()),
                "prioridad y hora programada");
    }

    private static Apartamento apartamento(String codigo, int capacidad, double tarifa,
            LocalTime hora) {
        return new Apartamento(codigo, "Apartamento " + codigo, "A", 1,
                2, capacidad, "", tarifa, EstadoOperativo.PREPARADO, null, hora);
    }

    private static void ordenar(Apartamento[] elementos, Comparator<Apartamento> comparador) {
        for (int i = 0; i < elementos.length - 1; i++) {
            for (int j = 0; j < elementos.length - 1 - i; j++) {
                if (comparador.compare(elementos[j], elementos[j + 1]) > 0) {
                    Apartamento temporal = elementos[j];
                    elementos[j] = elementos[j + 1];
                    elementos[j + 1] = temporal;
                }
            }
        }
    }

    private static void verificar(boolean condicion, String caso) {
        if (!condicion) {
            throw new AssertionError("Falló la prueba: " + caso);
        }
    }
}
