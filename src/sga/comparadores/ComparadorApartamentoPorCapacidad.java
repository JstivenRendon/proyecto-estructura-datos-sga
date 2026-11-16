package comparadores;

import java.util.Comparator;

import modelo.Apartamento;

public class ComparadorApartamentoPorCapacidad implements Comparator<Apartamento> {
    @Override
    public int compare(Apartamento primero, Apartamento segundo) {
        return Integer.compare(segundo.getCapacidadMaxima(), primero.getCapacidadMaxima());
    }
}
