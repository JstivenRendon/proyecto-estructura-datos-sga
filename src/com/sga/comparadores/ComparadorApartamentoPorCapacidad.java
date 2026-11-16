package com.sga.comparadores;

import com.sga.modelo.Apartamento;

import java.util.Comparator;

public class ComparadorApartamentoPorCapacidad implements Comparator<Apartamento> {
    @Override
    public int compare(Apartamento primero, Apartamento segundo) {
        return Integer.compare(segundo.getCapacidadMaxima(), primero.getCapacidadMaxima());
    }
}
