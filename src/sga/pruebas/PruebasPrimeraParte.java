package pruebas;

import estructuras.Conjunto;
import estructuras.Pila;
import excepciones.EstructuraVaciaException;
import modelo.Folio;
import modelo.Huesped;
import modelo.Recepcionista;
import modelo.SolicitudExterna;
import servicios.ServicioRecepcion;

import java.time.LocalDate;

public class PruebasPrimeraParte {

        private static int ok;
        private static int fail;
        public static void main(String[] args) {
            pruebaColaFifo();
            pruebaPilaLifo();
            pruebaConjuntoSinRepetidos();
            pruebaFolioDeshacer();
            pruebaCanalExternoYTurnos();
            System.out.println("Pruebas OK=" + ok + " FALLIDAS=" + fail);
            if (fail > 0) {
                System.exit(1);
            }
        }
        private static void pruebaColaFifo() {
            Cola<Integer> cola = new Cola<>();
            cola.encolar(1);
            cola.encolar(2);
            afirmar("FIFO", cola.desencolar() == 1 && cola.desencolar() == 2);
            try {
                cola.desencolar();
                afirmar("cola vacía debía fallar", false);
            } catch (EstructuraVaciaException e) {
                afirmar("excepción cola vacía", true);
            }
        }
        private static void pruebaPilaLifo() {
            Pila<String> pila = new Pila<>();
            pila.apilar("a");
            pila.apilar("b");
            afirmar("LIFO", "b".equals(pila.desapilar()) && "a".equals(pila.desapilar()));
        }
        private static void pruebaConjuntoSinRepetidos() {
            Conjunto<String> favoritos = new Conjunto<>();
            afirmar("primer favorito", favoritos.agregar("A102"));
            afirmar("no duplica favorito", !favoritos.agregar("A102") && favoritos.tamano() == 1);
        }
        private static void pruebaFolioDeshacer() {
            Folio folio = new Folio();
            folio.registrarCargo("Minibar", 10000);
            folio.registrarPago("Abono", 4000);
            afirmar("saldo después de cargo y pago", folio.saldo() == 6000);
            folio.deshacerUltimaOperacion();
            afirmar("deshacer no borra, revierte", folio.getMovimientos().tamano() == 3 && folio.saldo() == 10000);
            Huesped h = new Huesped("9", "X", LocalDate.now(), "", "");
            h.agregarAlHistorial("estancia 1");
            afirmar("historial enlazado", h.getHistorial().tamano() == 1);
        }
        private static void pruebaCanalExternoYTurnos() {
            ServicioRecepcion servicio = new ServicioRecepcion();
            servicio.registrarRecepcionista(new Recepcionista("R1", "Marta"));
            servicio.registrarRecepcionista(new Recepcionista("R2", "Julián"));
            servicio.encolarExterna(new SolicitudExterna("Booking", "1", "A101",
                    LocalDate.now(), LocalDate.now().plusDays(2), 2));
            String resultado = servicio.procesarSiguienteExterna();
            afirmar("procesa FIFO", resultado.contains("Booking") && servicio.getCanalExterno().estaVacia());
            afirmar("avanza turno circular", "Julián".equals(servicio.avanzarTurno().getNombre()));
        }
        private static void afirmar(String nombre, boolean condicion) {
            if (condicion) {
                ok++;
                System.out.println("OK  " + nombre);
            } else {
                fail++;
                System.out.println("FAIL " + nombre);
            }
        }
    }


