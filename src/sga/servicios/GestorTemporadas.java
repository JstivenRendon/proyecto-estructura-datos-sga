package servicios;

import java.time.LocalDate;

import modelo.Apartamento;
import modelo.RangoTemporada;
import modelo.Temporada;
import modelo.TipoTemporada;

public class GestorTemporadas {
    private static final int CAPACIDAD_INICIAL = 8;

    private RangoTemporada[] rangos = new RangoTemporada[CAPACIDAD_INICIAL];
    private int cantidad = 0;

    public GestorTemporadas() {
        registrarRango(new RangoTemporada(
                new Temporada(TipoTemporada.BAJA, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31)),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1.0));
        registrarRango(new RangoTemporada(
                new Temporada(TipoTemporada.MEDIA, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 6, 30)),
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 6, 30), 1.25));
        registrarRango(new RangoTemporada(
                new Temporada(TipoTemporada.ALTA, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 31)),
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 31), 1.45));
    }

    public void registrarRango(Temporada temporada, LocalDate fechaInicio, LocalDate fechaFin, double multiplicadorTarifa) {
        registrarRango(new RangoTemporada(temporada, fechaInicio, fechaFin, multiplicadorTarifa));
    }

    public void actualizarTarifaBase(Apartamento apartamento, double nuevaTarifa) {
        if (apartamento != null) {
            apartamento.setTarifaBasePorPersona(nuevaTarifa);
        }
    }

    public void actualizarRangoTemporada(Temporada temporada, LocalDate fechaInicio, LocalDate fechaFin, double multiplicadorTarifa) {
        registrarRango(temporada, fechaInicio, fechaFin, multiplicadorTarifa);
    }

    public void registrarRango(RangoTemporada rango) {
        if (rango == null) {
            throw new IllegalArgumentException("El rango no puede ser null");
        }
        if (cantidad == rangos.length) {
            crecer();
        }
        rangos[cantidad] = rango;
        cantidad++;
    }

    public Temporada obtenerTemporada(LocalDate fecha) {
        if (fecha == null) {
            return new Temporada(TipoTemporada.BAJA, LocalDate.MIN, LocalDate.MIN);
        }
        for (int i = 0; i < cantidad; i++) {
            if (rangos[i].cubre(fecha)) {
                return rangos[i].getTemporada();
            }
        }
        return new Temporada(TipoTemporada.BAJA, LocalDate.MIN, LocalDate.MIN);
    }

    public double obtenerMultiplicadorTarifa(Temporada temporada) {
        if (temporada == null) {
            return 1.0;
        }
        for (int i = 0; i < cantidad; i++) {
            if (rangos[i].getTemporada().getTipo() == temporada.getTipo()) {
                return rangos[i].getMultiplicadorTarifa();
            }
        }
        return 1.0;
    }

    public RangoTemporada[] getRangos() {
        RangoTemporada[] copia = new RangoTemporada[cantidad];
        for (int i = 0; i < cantidad; i++) {
            copia[i] = rangos[i];
        }
        return copia;
    }

    private void crecer() {
        RangoTemporada[] nuevo = new RangoTemporada[rangos.length * 2];
        for (int i = 0; i < rangos.length; i++) {
            nuevo[i] = rangos[i];
        }
        rangos = nuevo;
    }
}
