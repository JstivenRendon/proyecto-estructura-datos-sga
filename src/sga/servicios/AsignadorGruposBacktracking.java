package servicios;

import estructuras.ArbolBinarioBusqueda;
import estructuras.ListaEnlazada;
import excepciones.CapacidadExcedidaException;
import modelo.Apartamento;
import modelo.Estancia;
import modelo.EstadoOperativo;

public class AsignadorGruposBacktracking {
    private final ArbolBinarioBusqueda<Apartamento> catalogoApartamentos;

    public AsignadorGruposBacktracking(ArbolBinarioBusqueda<Apartamento> catalogoApartamentos) {
        this.catalogoApartamentos = catalogoApartamentos;
    }

    public ListaEnlazada<Apartamento> asignarGrupoGrande(Estancia estancia, int totalPersonas)
            throws CapacidadExcedidaException {
        if (estancia == null) {
            throw new IllegalArgumentException("La estancia no puede ser null");
        }
        ListaEnlazada<Apartamento> disponibles = obtenerDisponibles(estancia);
        if (disponibles.estaVacia()) {
            throw new CapacidadExcedidaException("No hay apartamentos disponibles para el grupo");
        }
        for (Apartamento apartamento : disponibles) {
            if (apartamento.getCapacidadMaxima() >= totalPersonas) {
                ListaEnlazada<Apartamento> resultado = new ListaEnlazada<>();
                resultado.agregarFinal(apartamento);
                return resultado;
            }
        }

        ListaEnlazada<Apartamento> mejor = new ListaEnlazada<>();
        int[] mejorConteo = new int[] { Integer.MAX_VALUE };
        Apartamento[] opciones = convertir(disponibles);
        ListaEnlazada<Apartamento> actual = new ListaEnlazada<>();
        resolverBacktracking(opciones, 0, totalPersonas, actual, mejor, mejorConteo, 0);

        if (mejor.estaVacia()) {
            throw new CapacidadExcedidaException("Ninguna combinación alcanza la capacidad requerida");
        }
        return mejor;
    }

    public Apartamento[] asignarGrupoGrandeArray(Estancia estancia, int totalPersonas)
            throws CapacidadExcedidaException {
        ListaEnlazada<Apartamento> combinacion = asignarGrupoGrande(estancia, totalPersonas);
        Apartamento[] resultado = new Apartamento[combinacion.tamano()];
        int indice = 0;
        for (Apartamento apartamento : combinacion) {
            resultado[indice] = apartamento;
            indice++;
        }
        return resultado;
    }

    private void resolverBacktracking(Apartamento[] opciones, int indice, int faltan,
            ListaEnlazada<Apartamento> actual, ListaEnlazada<Apartamento> mejor,
            int[] mejorConteo, int conteoActual) {
        if (faltan <= 0) {
            if (conteoActual < mejorConteo[0]) {
                mejorConteo[0] = conteoActual;
                mejor.limpiar();
                for (Apartamento apartamento : actual) {
                    mejor.agregarFinal(apartamento);
                }
            }
            return;
        }
        if (indice >= opciones.length) {
            return;
        }
        if (conteoActual >= mejorConteo[0]) {
            return;
        }

        for (int i = indice; i < opciones.length; i++) {
            Apartamento actualApto = opciones[i];
            if (actualApto.getCapacidadMaxima() <= 0) {
                continue;
            }
            actual.agregarFinal(actualApto);
            int nuevoFaltan = faltan - actualApto.getCapacidadMaxima();
            resolverBacktracking(opciones, i + 1, nuevoFaltan, actual, mejor, mejorConteo, conteoActual + 1);
            actual.eliminar(actual.tamano() - 1);
            if (actual.tamano() >= mejorConteo[0]) {
                continue;
            }
        }
    }

    private ListaEnlazada<Apartamento> obtenerDisponibles(Estancia estancia) {
        ListaEnlazada<Apartamento> disponibles = new ListaEnlazada<>();
        for (Apartamento apartamento : catalogoApartamentos) {
            if (apartamento.getEstadoOperativo() == EstadoOperativo.FUERA_DE_SERVICIO) {
                continue;
            }
            if (!apartamento.disponibleEn(estancia)) {
                continue;
            }
            disponibles.agregarFinal(apartamento);
        }
        return disponibles;
    }

    private Apartamento[] convertir(ListaEnlazada<Apartamento> lista) {
        Apartamento[] resultado = new Apartamento[lista.tamano()];
        int indice = 0;
        for (Apartamento apartamento : lista) {
            resultado[indice] = apartamento;
            indice++;
        }
        return resultado;
    }
}
