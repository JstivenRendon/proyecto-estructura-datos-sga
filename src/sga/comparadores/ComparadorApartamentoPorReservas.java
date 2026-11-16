package comparadores;

import java.util.Comparator;

import modelo.Apartamento;

public class ComparadorApartamentoPorReservas implements Comparator<Apartamento> {
    @Override
    public int compare(Apartamento primero, Apartamento segundo) {
        return Integer.compare(primero.getReservas().contarElementos(),
                segundo.getReservas().contarElementos());
    }
}
