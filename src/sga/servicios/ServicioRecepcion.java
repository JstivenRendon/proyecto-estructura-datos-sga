package servicios;


import estructuras.Cola;
import estructuras.ListaCircular;
import excepciones.EstructuraVaciaException;
import modelo.Recepcionista;
import modelo.SolicitudExterna;
    /**
     * Turnos de recepción (lista circular) y cola FIFO del canal externo.
     */
    public class ServicioRecepcion {
        private final ListaCircular<Recepcionista> turnos = new ListaCircular<>();
        private final Cola<SolicitudExterna> canalExterno = new Cola<>();
        public void registrarRecepcionista(Recepcionista recepcionista) {
            turnos.agregar(recepcionista);
        }
        public Recepcionista turnoActual() {
            return turnos.turnoActual();
        }
        public Recepcionista avanzarTurno() {
            return turnos.avanzarTurno();
        }
        public void encolarExterna(SolicitudExterna solicitud) {
            canalExterno.encolar(solicitud);
        }
        /**
         * Procesa la solicitud más antigua. El choque de fechas lo validará
         * más adelante el módulo de reservas (persona B).
         */
        public String procesarSiguienteExterna() {
            if (canalExterno.estaVacia()) {
                throw new EstructuraVaciaException("No hay solicitudes externas pendientes");
            }
            SolicitudExterna solicitud = canalExterno.desencolar();
            return "Procesada por " + turnoActual() + ": " + solicitud;
        }
        public Cola<SolicitudExterna> getCanalExterno() {
            return canalExterno;
        }
        public ListaCircular<Recepcionista> getTurnos() {
            return turnos;
        }
}
