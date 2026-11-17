package pruebas;

import java.time.LocalDate;

import comparadores.ComparadoresApartamento;
import estructuras.ListaEnlazada;
import excepciones.CapacidadExcedidaException;
import excepciones.ReservaSolapadaException;
import modelo.Apartamento;
import modelo.CanalReserva;
import modelo.EstadoReserva;
import modelo.Estancia;
import modelo.Reserva;
import modelo.TipoTemporada;
import persistencia.CargadorEscenario;
import servicios.SistemaAlojamiento;

public final class MainTestFlujoPrincipal {
    private MainTestFlujoPrincipal() {
    }

    public static void main(String[] args) throws Exception {
        SistemaAlojamiento sistema = CargadorEscenario.cargar();
        Apartamento apartamento = sistema.buscarApartamento("A101");
        Estancia estancia = new Estancia(LocalDate.now().plusDays(20), LocalDate.now().plusDays(23));

        Reserva directa = sistema.crearReserva("1001", "A101", estancia, CanalReserva.DIRECTA, 2);
        verificar(directa.getEstado() == EstadoReserva.CONFIRMADA, "la reserva directa se confirma");
        verificar(!contiene(sistema.buscarDisponibles(
                estancia, 2, ComparadoresApartamento.porPrecio(TipoTemporada.BAJA)), "A101"),
                "la disponibilidad excluye la reserva recién creada");

        sistema.cancelar(directa);
        verificar(contiene(sistema.buscarDisponibles(
                estancia, 2, ComparadoresApartamento.porPrecio(TipoTemporada.BAJA)), "A101"),
                "la cancelación libera las noches para disponibilidad");

        Reserva portal = sistema.crearReserva("1001", "A101", estancia, CanalReserva.PORTAL, 2);
        verificar(portal.getEstado() == EstadoReserva.PENDIENTE, "la reserva de portal queda pendiente");
        verificar(apartamento.getReservas().contarElementos() >= 3,
                "el árbol conserva reservas con la misma fecha de entrada");

        boolean rechazoSolapamiento = false;
        try {
            sistema.crearReserva("1003", "A101", estancia, CanalReserva.DIRECTA, 2);
        } catch (ReservaSolapadaException esperado) {
            rechazoSolapamiento = true;
        }
        verificar(rechazoSolapamiento, "se rechazan nuevas reservas solapadas");

        boolean rechazoCapacidad = false;
        try {
            sistema.crearReserva("1001", "A101", estancia, CanalReserva.DIRECTA, 5);
        } catch (CapacidadExcedidaException esperado) {
            rechazoCapacidad = true;
        }
        verificar(rechazoCapacidad, "se valida la capacidad antes de reservar");

        System.out.println("Flujo principal de reservas y disponibilidad verificado.");
    }

    private static boolean contiene(ListaEnlazada<Apartamento> apartamentos, String codigo) {
        for (Apartamento apartamento : apartamentos) {
            if (codigo.equals(apartamento.getCodigo())) {
                return true;
            }
        }
        return false;
    }

    private static void verificar(boolean condicion, String caso) {
        if (!condicion) {
            throw new AssertionError("Falló la prueba: " + caso);
        }
    }
}
