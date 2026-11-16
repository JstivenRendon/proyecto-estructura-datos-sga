package comparadores;

import java.util.Comparator;

import modelo.Apartamento;

public class ComparadorApartamentoPorPrecio implements Comparator<Apartamento> {
    @Override
    public int compare(Apartamento primero, Apartamento segundo) {
        return Double.compare(primero.getTarifaBasePorPersona(),
                segundo.getTarifaBasePorPersona());
    }
}
