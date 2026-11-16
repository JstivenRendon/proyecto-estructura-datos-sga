package com.sga.comparadores;

import com.sga.modelo.Apartamento;

import java.util.Comparator;

public class ComparadorApartamentoPorReservas implements Comparator<Apartamento> {
    @Override
    public int compare(Apartamento primero, Apartamento segundo) {
        return Integer.compare(primero.getReservas().contarElementos(),
                segundo.getReservas().contarElementos());
    }
}
