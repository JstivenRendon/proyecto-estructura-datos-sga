package com.sga.comparadores;

import com.sga.modelo.Apartamento;

import java.util.Comparator;

public class ComparadorApartamentoPorPrecio implements Comparator<Apartamento> {
    @Override
    public int compare(Apartamento primero, Apartamento segundo) {
        return Double.compare(primero.getTarifaBasePorPersona(),
                segundo.getTarifaBasePorPersona());
    }
}
