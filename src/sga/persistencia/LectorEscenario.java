package persistencia;

import excepciones.FechasInvalidasException;
import modelo.Apartamento;
import modelo.CanalReserva;
import modelo.EstadoOperativo;
import modelo.EstadoReserva;
import modelo.Estancia;
import modelo.Huesped;
import modelo.NodoEdificio;
import modelo.Recepcionista;
import modelo.Reserva;
import modelo.SolicitudExterna;
import modelo.TareaPreparacion;
import modelo.Temporada;
import modelo.TipoTemporada;
import servicios.SistemaAlojamiento;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Lee un escenario de prueba desde un archivo de texto plano.
 *
 * Cada línea es un registro con los campos separados por ";". Las líneas
 * vacías y las que empiezan con "#" se ignoran. Como cada registro usa datos
 * de los anteriores, el archivo debe respetar este orden:
 *
 *   APARTAMENTO;codigo;nombre;bloque;piso;dormitorios;capacidad;caracteristicas;tarifa;estado;motivo;horaLlegadaHoy
 *   HUESPED;documento;nombre;fechaNacimiento;telefono;correo
 *   RECEPCIONISTA;id;nombre
 *   TEMPORADA;tipo;inicio;fin
 *   NODO;id;tipo;codigoApartamento
 *   PASILLO;origen;destino;peso
 *   RESERVA;id;documento;codigoApartamento;entrada;salida;canal;estado;personas
 *   FAVORITO;documento;codigoApartamento
 *   SOLICITUD_EXTERNA;plataforma;documento;codigoApartamento;entrada;salida;personas
 *   PREPARACION;codigoApartamento;entradaHoy;horaLlegada
 *
 * Las fechas se escriben como 2026-11-20 o como un desplazamiento respecto a
 * hoy (+2, -1, 0), así el escenario no se desactualiza.
 */
public final class LectorEscenario {
    private LectorEscenario() {
    }

    public static SistemaAlojamiento cargar(String ruta) throws IOException {
        SistemaAlojamiento sistema = new SistemaAlojamiento("Aparta-Suites UQ");
        try (BufferedReader lector = Files.newBufferedReader(Paths.get(ruta), StandardCharsets.UTF_8)) {
            String linea;
            int numero = 0;
            while ((linea = lector.readLine()) != null) {
                numero++;
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }
                try {
                    procesarLinea(sistema, linea.split(";", -1));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Línea " + numero + " (" + linea + "): " + e.getMessage(), e);
                }
            }
        }
        return sistema;
    }

    private static void procesarLinea(SistemaAlojamiento sistema, String[] campos) throws FechasInvalidasException {
        switch (campos[0]) {
            case "APARTAMENTO":
                exigirCampos(campos, 12);
                sistema.registrarApartamento(new Apartamento(campos[1], campos[2], campos[3],
                        Integer.parseInt(campos[4]), Integer.parseInt(campos[5]), Integer.parseInt(campos[6]),
                        campos[7], Double.parseDouble(campos[8]), EstadoOperativo.valueOf(campos[9]),
                        campos[10].isEmpty() ? null : campos[10], hora(campos[11])));
                break;
            case "HUESPED":
                exigirCampos(campos, 6);
                sistema.registrarHuesped(new Huesped(campos[2], campos[1], LocalDate.parse(campos[3]),
                        campos[4], campos[5], null));
                break;
            case "RECEPCIONISTA":
                exigirCampos(campos, 3);
                sistema.registrarRecepcionista(new Recepcionista(campos[1], campos[2]));
                break;
            case "TEMPORADA":
                exigirCampos(campos, 4);
                sistema.registrarTemporada(new Temporada(TipoTemporada.valueOf(campos[1]),
                        LocalDate.parse(campos[2]), LocalDate.parse(campos[3])));
                break;
            case "NODO":
                exigirCampos(campos, 4);
                registrarNodo(sistema, campos);
                break;
            case "PASILLO":
                exigirCampos(campos, 4);
                sistema.registrarPasillo(campos[1], campos[2], Double.parseDouble(campos[3]));
                break;
            case "RESERVA":
                exigirCampos(campos, 9);
                registrarReserva(sistema, campos);
                break;
            case "FAVORITO":
                exigirCampos(campos, 3);
                huesped(sistema, campos[1]).agregarFavorito(apartamento(sistema, campos[2]).getCodigo());
                break;
            case "SOLICITUD_EXTERNA":
                exigirCampos(campos, 7);
                apartamento(sistema, campos[3]);
                sistema.encolarExterna(new SolicitudExterna(campos[1], campos[2], campos[3],
                        new Estancia(fecha(campos[4]), fecha(campos[5])), Integer.parseInt(campos[6])));
                break;
            case "PREPARACION":
                exigirCampos(campos, 4);
                boolean entradaHoy = Boolean.parseBoolean(campos[2]);
                LocalTime horaLlegada = campos[3].isEmpty() ? LocalTime.MAX : LocalTime.parse(campos[3]);
                sistema.getColaPreparacion().encolar(
                        new TareaPreparacion(apartamento(sistema, campos[1]), entradaHoy, horaLlegada));
                break;
            default:
                throw new IllegalArgumentException("tipo de registro desconocido: " + campos[0]);
        }
    }

    private static void registrarNodo(SistemaAlojamiento sistema, String[] campos) {
        NodoEdificio.Tipo tipo = NodoEdificio.Tipo.valueOf(campos[2]);
        Apartamento apartamento = null;
        if (tipo == NodoEdificio.Tipo.APARTAMENTO) {
            apartamento = apartamento(sistema, campos[3]);
        }
        sistema.registrarNodoMapa(new NodoEdificio(campos[1], tipo, apartamento));
    }

    private static void registrarReserva(SistemaAlojamiento sistema, String[] campos) throws FechasInvalidasException {
        Huesped titular = huesped(sistema, campos[2]);
        Apartamento apartamento = apartamento(sistema, campos[3]);
        Estancia estancia = new Estancia(fecha(campos[4]), fecha(campos[5]));
        EstadoReserva estado = EstadoReserva.valueOf(campos[7]);
        boolean activa = estado != EstadoReserva.CANCELADA && estado != EstadoReserva.NO_SHOW;
        if (activa && apartamento.haySolapamiento(estancia)) {
            throw new IllegalArgumentException("la reserva se solapa con otra del apartamento " + campos[3]);
        }
        Reserva reserva = new Reserva(campos[1], titular, apartamento, estancia,
                CanalReserva.valueOf(campos[6]), estado, LocalDateTime.now(), Integer.parseInt(campos[8]));
        apartamento.agregarReserva(reserva);
        sistema.getReservas().agregarFinal(reserva);
        titular.agregarAlHistorial(reserva);
    }

    // -------------------------------------------------------------------- apoyo

    private static void exigirCampos(String[] campos, int esperados) {
        if (campos.length != esperados) {
            throw new IllegalArgumentException(campos[0] + " necesita " + esperados
                    + " campos y tiene " + campos.length);
        }
    }

    private static Apartamento apartamento(SistemaAlojamiento sistema, String codigo) {
        Apartamento apartamento = sistema.buscarApartamento(codigo);
        if (apartamento == null) {
            throw new IllegalArgumentException("no existe el apartamento " + codigo);
        }
        return apartamento;
    }

    private static Huesped huesped(SistemaAlojamiento sistema, String documento) {
        Huesped huesped = sistema.buscarHuesped(documento);
        if (huesped == null) {
            throw new IllegalArgumentException("no existe el huésped " + documento);
        }
        return huesped;
    }

    /** Acepta una fecha completa (2026-11-20) o un desplazamiento respecto a hoy (+2, -1, 0). */
    private static LocalDate fecha(String texto) {
        if (texto.matches("[+-]?\\d{1,3}")) {
            return LocalDate.now().plusDays(Integer.parseInt(texto.replace("+", "")));
        }
        return LocalDate.parse(texto);
    }

    private static LocalTime hora(String texto) {
        return texto.isEmpty() ? null : LocalTime.parse(texto);
    }
}