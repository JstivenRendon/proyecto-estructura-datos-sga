package servicios;

import comparadores.ComparadorPreparacion;
import comparadores.ComparadoresApartamento;
import estructuras.*;
import excepciones.CapacidadExcedidaException;
import excepciones.FechasInvalidasException;
import excepciones.ReservaSolapadaException;
import excepciones.TransicionEstadoInvalidaException;
import modelo.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;

/**
 * Fachada del SGA: un único alojamiento con todas las operaciones del enunciado.
 */
public class SistemaAlojamiento {
    private final String nombre;
    private final ArbolBinarioBusqueda<Apartamento> apartamentos = new ArbolBinarioBusqueda<>();
    private final ArbolBinarioBusqueda<Huesped> huespedes = new ArbolBinarioBusqueda<>();
    private final ListaEnlazada<Reserva> reservas = new ListaEnlazada<>();
    private final ListaEnlazada<String> conflictosCanal = new ListaEnlazada<>();
    private final ListaEnlazada<Temporada> temporadas = new ListaEnlazada<>();
    private final Cola<SolicitudExterna> canalExterno = new Cola<>();
    private final ColaPrioridad<TareaPreparacion> colaPreparacion = new ColaPrioridad<>(ComparadorPreparacion.crear());
    private final ListaEnlazada<TareaPreparacion> preparacionesTerminadas = new ListaEnlazada<>();
    private final ColaPrioridad<Reserva> pendientesPorVencer = new ColaPrioridad<>(
            Comparator.comparing(Reserva::getVenceEn)
    );
    private final ListaCircular<Recepcionista> turnos = new ListaCircular<>();
    private final Grafo<NodoEdificio> mapa = new Grafo<>();
    private final ListaEnlazada<NodoEdificio> nodosMapa = new ListaEnlazada<>();
    private NodoEdificio pasilloCerradoA;
    private NodoEdificio pasilloCerradoB;

    public SistemaAlojamiento(String nombre) {
        this.nombre = nombre;
    }

    public void registrarApartamento(Apartamento apartamento) {
        apartamentos.insertar(apartamento);
    }

    public void registrarHuesped(Huesped huesped) {
        huespedes.insertar(huesped);
    }

    public void registrarReservaInicial(Reserva reserva) throws ReservaSolapadaException {
        if (reserva == null || reserva.getApartamento() == null || reserva.getTitular() == null
                || reserva.getEstancia() == null) {
            throw new IllegalArgumentException("La reserva inicial debe tener apartamento, huésped y estancia");
        }
        Apartamento apartamento = buscarApartamento(reserva.getApartamento().getCodigo());
        Huesped huesped = buscarHuesped(reserva.getTitular().getDocumento());
        if (apartamento != reserva.getApartamento() || huesped != reserva.getTitular()) {
            throw new IllegalArgumentException("El apartamento y el huésped deben estar registrados");
        }
        if (reserva.getIdReserva() == null || buscarReservaPorId(reserva.getIdReserva()) != null) {
            throw new IllegalArgumentException("La reserva debe tener un identificador único");
        }
        if (apartamento.existeSolapamiento(reserva.getEstancia())) {
            throw new ReservaSolapadaException("La reserva inicial se solapa con una reserva activa");
        }

        apartamento.agregarReserva(reserva);
        reservas.agregarFinal(reserva);
        huesped.agregarAlHistorial(reserva);
        if (reserva.getEstado() == EstadoReserva.PENDIENTE) {
            pendientesPorVencer.encolar(reserva);
        }
    }

    public void registrarRecepcionista(Recepcionista recepcionista) {
        turnos.agregar(recepcionista);
    }

    public void registrarTemporada(Temporada temporada) {
        temporadas.agregarFinal(temporada);
    }

    public void registrarNodoMapa(NodoEdificio nodo) {
        nodosMapa.agregarFinal(nodo);
        mapa.agregarVertice(nodo);
    }

    public void registrarPasillo(String idOrigen, String idDestino, double peso) {
        mapa.agregarArista(buscarNodo(idOrigen), buscarNodo(idDestino), peso);
    }

    public Huesped buscarHuesped(String documento) {
        return huespedes.buscar(new Huesped(documento, "", LocalDate.now(), "", ""));
    }

    public Apartamento buscarApartamento(String codigo) {
        return apartamentos.buscar(new Apartamento(codigo, "", "", 0, 0, 0,
                "", 0.0, EstadoOperativo.PREPARADO, null, null));
    }

    public ListaEnlazada<Apartamento> buscarDisponibles(Estancia estancia, int personas, Comparator<Apartamento> orden) {
        if (estancia == null) {
            throw new IllegalArgumentException("La estancia no puede ser null");
        }
        if (personas < 1) {
            throw new IllegalArgumentException("La cantidad de personas debe ser mayor que cero");
        }
        ListaEnlazada<Apartamento> resultado = new ListaEnlazada<>();
        for (Apartamento apto : apartamentos) {
            if (apto.getCapacidadMaxima() >= personas && apto.disponibleEn(estancia)
                    && apto.getEstado() != EstadoApartamento.FUERA_DE_SERVICIO) {
                insertarOrdenado(resultado, apto, orden);
            }
        }
        return resultado;
    }

    public ListaEnlazada<NocheLiquidada> cotizar(Apartamento apartamento, Reserva reserva) {
        return liquidar(apartamento, reserva.getEstancia(), reserva);
    }

    public ListaEnlazada<NocheLiquidada> liquidar(Apartamento apartamento, Estancia estancia, Reserva reserva) {
        ListaEnlazada<NocheLiquidada> desglose = new ListaEnlazada<>();
        liquidarRec(apartamento, estancia.iterator(), reserva, desglose);
        return desglose;
    }

    private void liquidarRec(Apartamento apartamento, java.util.Iterator<LocalDate> noches,
                             Reserva reserva, ListaEnlazada<NocheLiquidada> desglose) {
        if (!noches.hasNext()) {
            return;
        }
        LocalDate noche = noches.next();
        TipoTemporada temporada = temporadaDe(noche);
        int pagan = reserva == null ? 1 : reserva.personasQuePagan(noche);
        desglose.agregarFinal(new NocheLiquidada(noche, temporada, pagan, apartamento.tarifa(temporada)));
        liquidarRec(apartamento, noches, reserva, desglose);
    }

    public Reserva crearReserva(String documento, String codigoApartamento, Estancia estancia, CanalReserva canal)
            throws ReservaSolapadaException, CapacidadExcedidaException, TransicionEstadoInvalidaException {
        return crearReserva(documento, codigoApartamento, estancia, canal, 1);
    }

    public Reserva crearReserva(String documento, String codigoApartamento, Estancia estancia, CanalReserva canal,
            int cantidadOcupantes)
            throws ReservaSolapadaException, CapacidadExcedidaException, TransicionEstadoInvalidaException {
        if (estancia == null || canal == null) {
            throw new IllegalArgumentException("La estancia y el canal son obligatorios");
        }
        if (cantidadOcupantes < 1) {
            throw new IllegalArgumentException("La cantidad de ocupantes debe ser mayor que cero");
        }
        Huesped huesped = buscarHuesped(documento);
        Apartamento apartamento = buscarApartamento(codigoApartamento);
        if (huesped == null || apartamento == null) {
            throw new IllegalArgumentException("Huésped o apartamento no encontrado");
        }
        if (apartamento.getCapacidadMaxima() < cantidadOcupantes) {
            throw new CapacidadExcedidaException("La cantidad de ocupantes excede la capacidad del apartamento");
        }
        if (apartamento.getEstadoOperativo() == EstadoOperativo.FUERA_DE_SERVICIO) {
            throw new TransicionEstadoInvalidaException("El apartamento está fuera de servicio");
        }
        if (!apartamento.disponibleEn(estancia)) {
            throw new ReservaSolapadaException("El apartamento ya tiene reservas activas para esas fechas");
        }

        EstadoReserva estadoInicial = canal == CanalReserva.PORTAL
                ? EstadoReserva.PENDIENTE
                : EstadoReserva.CONFIRMADA;
        Reserva reserva = new Reserva(generarIdReserva(), huesped, apartamento, estancia, canal,
                estadoInicial, LocalDateTime.now(), cantidadOcupantes);
        registrarReservaInicial(reserva);
        return reserva;
    }

    public void pagarAnticipo(Reserva reserva, double valorEstancia)
            throws TransicionEstadoInvalidaException {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser null");
        }
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new TransicionEstadoInvalidaException("Solo se puede pagar el anticipo de una reserva pendiente");
        }
        reserva.getFolio().registrarPago("Anticipo 30%", valorEstancia * 0.30);
        reserva.transicionarA(EstadoReserva.CONFIRMADA);
    }

    public double valorDevolucion(Reserva reserva) {
        double pagado = 0;
        for (MovimientoFolio movimiento : reserva.getFolio().getMovimientos()) {
            if (movimiento.getValor() < 0) {
                pagado += -movimiento.getValor();
            }
        }
        return pagado * 0.80;
    }

    public void cancelar(Reserva reserva) throws TransicionEstadoInvalidaException {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser null");
        }
        if (reserva.getEstado() != EstadoReserva.PENDIENTE
                && reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new TransicionEstadoInvalidaException("Solo se pueden cancelar reservas pendientes o confirmadas");
        }
        reserva.transicionarA(EstadoReserva.CANCELADA);
    }

    public void checkIn(Reserva reserva) throws TransicionEstadoInvalidaException {
        if (reserva.getApartamento().getEstado() != EstadoApartamento.PREPARADO) {
            throw new TransicionEstadoInvalidaException("El check-in solo procede si el apartamento está Preparado");
        }
        reserva.transicionarA(EstadoReserva.EN_CURSO);
        reserva.getApartamento().cambiarEstado(EstadoApartamento.OCUPADO);
    }

    public void checkOut(Reserva reserva) throws TransicionEstadoInvalidaException {
        reserva.transicionarA(EstadoReserva.FINALIZADA);
        Apartamento apto = reserva.getApartamento();
        apto.cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION);
        Reserva siguiente = apto.reservaConEntrada(LocalDate.now());
        boolean entradaHoy = siguiente != null;
        LocalTime hora = entradaHoy ? LocalTime.of(15, 0) : LocalTime.MAX;
        colaPreparacion.encolar(new TareaPreparacion(apto, entradaHoy, hora));
    }

    public TareaPreparacion despacharPreparacion() throws TransicionEstadoInvalidaException {
        // Si un apartamento quedó fuera de servicio, su tarea de aseo ya no aplica
        while (!colaPreparacion.estaVacia()
                && colaPreparacion.frente().getApartamento().getEstado() == EstadoApartamento.FUERA_DE_SERVICIO) {
            colaPreparacion.desencolar();
        }
        // Primero se mira el frente; solo se saca de la cola si el cambio de estado fue válido
        TareaPreparacion tarea = colaPreparacion.frente();
        tarea.getApartamento().cambiarEstado(EstadoApartamento.EN_PREPARACION);
        tarea.getApartamento().cambiarEstado(EstadoApartamento.PREPARADO);
        colaPreparacion.desencolar();
        tarea.terminar();
        preparacionesTerminadas.agregarFinal(tarea);
        return tarea;
    }

    /**
     * Ruta de aseo: sale del cuarto de aseo (o de recepción si no hay uno) y
     * visita los apartamentos pendientes en el orden de la cola de prioridad.
     * Cada tramo entre dos apartamentos se calcula con Dijkstra.
     */
    public ListaEnlazada<NodoEdificio> rutaPreparacion() {
        ListaEnlazada<NodoEdificio> rutaTotal = new ListaEnlazada<>();
        if (colaPreparacion.estaVacia()) {
            return rutaTotal;
        }

        NodoEdificio actual = buscarNodo("ASEO");
        if (actual == null) {
            actual = buscarNodoPorTipo(NodoEdificio.Tipo.RECEPCION);
        }
        if (actual == null) {
            return rutaTotal;
        }
        rutaTotal.agregarFinal(actual);

        // Si hay un pasillo cerrado, Dijkstra trabaja sobre el grafo sin ese pasillo
        Grafo<NodoEdificio> grafo = grafoVigente();

        // El for-each recorre la cola en orden de prioridad sin sacar los elementos
        for (TareaPreparacion tarea : colaPreparacion) {
            NodoEdificio destino = buscarNodoApartamento(tarea.getApartamento().getCodigo());
            if (destino == null || tarea.getApartamento().getEstado() == EstadoApartamento.FUERA_DE_SERVICIO) {
                continue;
            }
            ListaEnlazada<NodoEdificio> tramo = grafo.dijkstra(actual, destino);
            boolean esPrimero = true;
            for (NodoEdificio nodo : tramo) {
                if (esPrimero) {
                    esPrimero = false; // el primer nodo del tramo ya está en la ruta
                } else {
                    rutaTotal.agregarFinal(nodo);
                }
            }
            if (!tramo.estaVacia()) {
                actual = destino;
            }
        }
        return rutaTotal;
    }

    public String procesarSiguienteExterna() throws FechasInvalidasException {
        if (canalExterno.estaVacia()) {
            return "No hay solicitudes externas pendientes";
        }
        SolicitudExterna solicitud = canalExterno.desencolar();
        Apartamento apto = buscarApartamento(solicitud.getCodigoApartamento());
        if (apto == null || apto.haySolapamiento(solicitud.getEstancia())) {
            String conflicto = "Conflicto canal externo: " + solicitud;
            conflictosCanal.agregarFinal(conflicto);
            return conflicto;
        }
        try {
            Reserva reserva = crearReserva(solicitud.getDocumentoTitular(), solicitud.getCodigoApartamento(),
                    solicitud.getEstancia(), CanalReserva.EXTERNA, solicitud.getPersonas());
            return "Solicitud aceptada: " + reserva;
        } catch (Exception e) {
            String conflicto = "Conflicto canal externo: " + e.getMessage();
            conflictosCanal.agregarFinal(conflicto);
            return conflicto;
        }
    }

    public void encolarExterna(SolicitudExterna solicitud) {
        canalExterno.encolar(solicitud);
    }

    public int cancelarPendientesVencidas() {
        int canceladas = 0;
        ColaPrioridad<Reserva> vigentes = new ColaPrioridad<>(Comparator.comparing(Reserva::getVenceEn));
        while (!pendientesPorVencer.estaVacia()) {
            Reserva reserva = pendientesPorVencer.desencolar();
            if (reserva.getEstado() == EstadoReserva.PENDIENTE && !reserva.getVenceEn().isAfter(LocalDateTime.now())) {
                try {
                    reserva.transicionarA(EstadoReserva.CANCELADA);
                    canceladas++;
                } catch (TransicionEstadoInvalidaException ignored) {
                }
            } else if (reserva.getEstado() == EstadoReserva.PENDIENTE) {
                vigentes.encolar(reserva);
            }
        }
        while (!vigentes.estaVacia()) {
            pendientesPorVencer.encolar(vigentes.desencolar());
        }
        return canceladas;
    }

    /**
     * Backtracking: combinación de apartamentos que aloja al grupo con el menor número de unidades.
     */
    public ListaEnlazada<Apartamento> asignarGrupoGrande(Estancia estancia, int personas) {
        ListaEnlazada<Apartamento> candidatos = buscarDisponibles(estancia, 1, ComparadoresApartamento.porCapacidad().reversed());
        ListaEnlazada<Apartamento> mejor = new ListaEnlazada<>();
        backtrackGrupo(candidatos, 0, personas, new ListaEnlazada<>(), mejor);
        return mejor;
    }

    private void backtrackGrupo(ListaEnlazada<Apartamento> candidatos, int indice, int faltan,
                                ListaEnlazada<Apartamento> actual, ListaEnlazada<Apartamento> mejor) {
        if (faltan <= 0) {
            if (mejor.estaVacia() || actual.tamano() < mejor.tamano()) {
                mejor.limpiar();
                for (Apartamento apto : actual) {
                    mejor.agregarFinal(apto);
                }
            }
            return;
        }
        if (indice >= candidatos.tamano()) {
            return;
        }
        if (!mejor.estaVacia() && actual.tamano() >= mejor.tamano()) {
            return;
        }
        Apartamento candidato = candidatos.obtener(indice);
        actual.agregarFinal(candidato);
        backtrackGrupo(candidatos, indice + 1, faltan - candidato.getCapacidadMaxima(), actual, mejor);
        actual.eliminar(actual.tamano() - 1);
        backtrackGrupo(candidatos, indice + 1, faltan, actual, mejor);
    }

    public ListaEnlazada<Apartamento> dfsDesde(String idNodo) {
        ListaEnlazada<Apartamento> aptos = new ListaEnlazada<>();
        for (NodoEdificio nodo : mapa.dfs(buscarNodo(idNodo))) {
            if (nodo.getApartamento() != null) {
                aptos.agregarFinal(nodo.getApartamento());
            }
        }
        return aptos;
    }

    /** Suma los pesos (metros) de los pasillos que recorre la ruta. */
    public double distanciaRuta(ListaEnlazada<NodoEdificio> ruta) {
        double total = 0;
        NodoEdificio anterior = null;
        for (NodoEdificio nodo : ruta) {
            if (anterior != null) {
                for (Grafo.Arista<NodoEdificio> arista : mapa.adyacentes(anterior)) {
                    if (arista.getDestino().equals(nodo)) {
                        total += arista.getPeso();
                        break;
                    }
                }
            }
            anterior = nodo;
        }
        return total;
    }

    /**
     * Agrupa los apartamentos por bloque usando DFS (recursivo). Se parte de
     * cada escalera y solo se recorren apartamentos: las demás escaleras y la
     * recepción no se cruzan, así cada grupo queda dentro de su bloque.
     */
    public String reporteBloques() {
        StringBuilder sb = new StringBuilder("Apartamentos agrupados por bloque (DFS):\n");
        boolean hayEscaleras = false;

        for (NodoEdificio nodo : nodosMapa) {
            if (nodo.getTipo() == NodoEdificio.Tipo.SERVICIO && nodo.getId().startsWith("ESC")) {
                hayEscaleras = true;
                ListaEnlazada<Apartamento> grupo = new ListaEnlazada<>();
                Conjunto<NodoEdificio> visitados = new Conjunto<>();
                visitados.agregar(nodo);
                dfsBloque(nodo, visitados, grupo);

                if (grupo.estaVacia()) {
                    sb.append("\n").append(nodo.getId()).append(": sin apartamentos");
                } else {
                    sb.append("\nBloque ").append(grupo.obtener(0).getBloque())
                            .append(" (desde ").append(nodo.getId()).append("): ");
                    boolean primero = true;
                    for (Apartamento apto : grupo) {
                        if (!primero) {
                            sb.append(", ");
                        }
                        sb.append(apto.getCodigo());
                        primero = false;
                    }
                }
            }
        }
        if (!hayEscaleras) {
            sb.append("\nNo hay escaleras en el mapa");
        }
        return sb.toString();
    }

    private void dfsBloque(NodoEdificio actual, Conjunto<NodoEdificio> visitados,
                           ListaEnlazada<Apartamento> grupo) {
        for (Grafo.Arista<NodoEdificio> arista : mapa.adyacentes(actual)) {
            NodoEdificio vecino = arista.getDestino();
            if (vecino.getTipo() == NodoEdificio.Tipo.APARTAMENTO && !visitados.contiene(vecino)) {
                visitados.agregar(vecino);
                grupo.agregarFinal(vecino.getApartamento());
                dfsBloque(vecino, visitados, grupo);
            }
        }
    }

    /** Cierra un pasillo: desde ahora las rutas de aseo no pasan por él. */
    public void cerrarPasillo(String idA, String idB) {
        pasilloCerradoA = buscarNodo(idA);
        pasilloCerradoB = buscarNodo(idB);
    }

    /** Vuelve a abrir el pasillo cerrado. */
    public void abrirPasillo() {
        pasilloCerradoA = null;
        pasilloCerradoB = null;
    }

    /** Grafo que usan las rutas: el mapa completo, o el mapa sin el pasillo cerrado. */
    private Grafo<NodoEdificio> grafoVigente() {
        if (pasilloCerradoA == null || pasilloCerradoB == null) {
            return mapa;
        }
        return grafoSinPasillo(pasilloCerradoA, pasilloCerradoB);
    }

    /** Copia del mapa en la que se omite el pasillo entre a y b. */
    private Grafo<NodoEdificio> grafoSinPasillo(NodoEdificio a, NodoEdificio b) {
        Grafo<NodoEdificio> reducido = new Grafo<>();
        for (NodoEdificio nodo : nodosMapa) {
            reducido.agregarVertice(nodo);
        }
        for (NodoEdificio origen : nodosMapa) {
            for (Grafo.Arista<NodoEdificio> arista : mapa.adyacentes(origen)) {
                boolean esPasilloCerrado = (origen.equals(a) && arista.getDestino().equals(b))
                        || (origen.equals(b) && arista.getDestino().equals(a));
                // cada pasillo está guardado en los dos sentidos; se copia una sola vez
                if (!esPasilloCerrado && origen.getId().compareTo(arista.getDestino().getId()) < 0) {
                    reducido.agregarArista(origen, arista.getDestino(), arista.getPeso());
                }
            }
        }
        return reducido;
    }

    public boolean pasilloCierraAisla(String idPasilloA, String idPasilloB, String idObjetivo) {
        Grafo<NodoEdificio> reducido = grafoSinPasillo(buscarNodo(idPasilloA), buscarNodo(idPasilloB));
        NodoEdificio recepcion = buscarNodoPorTipo(NodoEdificio.Tipo.RECEPCION);
        return !reducido.alcanzable(recepcion, buscarNodo(idObjetivo));
    }

    public Recepcionista turnoActual() {
        return turnos.turnoActual();
    }

    public Recepcionista avanzarTurno() {
        return turnos.avanzarTurno();
    }

    public TipoTemporada temporadaDe(LocalDate noche) {
        for (Temporada temporada : temporadas) {
            if (temporada.cubre(noche)) {
                return temporada.getTipo();
            }
        }
        return TipoTemporada.BAJA;
    }

    public String getNombre() {
        return nombre;
    }

    public ArbolBinarioBusqueda<Apartamento> getApartamentos() {
        return apartamentos;
    }

    public ArbolBinarioBusqueda<Huesped> getHuespedes() {
        return huespedes;
    }

    public ListaEnlazada<Reserva> getReservas() {
        return reservas;
    }

    public Reserva buscarReservaPorId(String idReserva) {
        if (idReserva == null) {
            return null;
        }
        for (Reserva reserva : reservas) {
            if (idReserva.equals(reserva.getIdReserva())) {
                return reserva;
            }
        }
        return null;
    }

    public ListaEnlazada<String> getConflictosCanal() {
        return conflictosCanal;
    }

    public Cola<SolicitudExterna> getCanalExterno() {
        return canalExterno;
    }

    public ColaPrioridad<TareaPreparacion> getColaPreparacion() {
        return colaPreparacion;
    }

    public ListaEnlazada<TareaPreparacion> getPreparacionesTerminadas() {
        return preparacionesTerminadas;
    }

    public Grafo<NodoEdificio> getMapa() {
        return mapa;
    }

    public ListaEnlazada<NodoEdificio> getNodosMapa() {
        return nodosMapa;
    }

    public ListaCircular<Recepcionista> getTurnos() {
        return turnos;
    }

    private String generarIdReserva() {
        int consecutivo = reservas.tamano() + 1;
        String id;
        do {
            id = "RES-" + consecutivo;
            consecutivo++;
        } while (buscarReservaPorId(id) != null);
        return id;
    }

    private NodoEdificio buscarNodo(String id) {
        for (NodoEdificio nodo : nodosMapa) {
            if (nodo.getId().equals(id)) {
                return nodo;
            }
        }
        return null;
    }

    private NodoEdificio buscarNodoPorTipo(NodoEdificio.Tipo tipo) {
        for (NodoEdificio nodo : nodosMapa) {
            if (nodo.getTipo() == tipo) {
                return nodo;
            }
        }
        return null;
    }

    private NodoEdificio buscarNodoApartamento(String codigo) {
        for (NodoEdificio nodo : nodosMapa) {
            if (nodo.getApartamento() != null && nodo.getApartamento().getCodigo().equals(codigo)) {
                return nodo;
            }
        }
        return null;
    }

    private void insertarOrdenado(ListaEnlazada<Apartamento> lista, Apartamento apto, Comparator<Apartamento> orden) {
        if (orden == null) {
            lista.agregarFinal(apto);
            return;
        }
        ListaEnlazada<Apartamento> temporal = new ListaEnlazada<>();
        boolean puesto = false;
        for (Apartamento actual : lista) {
            if (!puesto && orden.compare(apto, actual) <= 0) {
                temporal.agregarFinal(apto);
                puesto = true;
            }
            temporal.agregarFinal(actual);
        }
        if (!puesto) {
            temporal.agregarFinal(apto);
        }
        lista.limpiar();
        for (Apartamento actual : temporal) {
            lista.agregarFinal(actual);
        }
    }
}