package comparadores;

import java.util.Comparator;

import modelo.Apartamento;
import modelo.TipoTemporada;

public final class ComparadoresApartamento {
    private ComparadoresApartamento() {
    }

    public static Comparator<Apartamento> porPrecio(TipoTemporada temporada) {
        return Comparator.comparingDouble(apartamento -> apartamento.tarifa(temporada));
    }

    public static Comparator<Apartamento> porCapacidad() {
        return Comparator.comparingInt(Apartamento::getCapacidadMaxima).reversed();
    }

    public static Comparator<Apartamento> porDormitorios() {
        return Comparator.comparingInt(Apartamento::getNumeroDormitorios).reversed();
    }
}
