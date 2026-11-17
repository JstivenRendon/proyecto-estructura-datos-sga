package modelo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TareaPreparacion {
    private final Apartamento apartamento;
    private final boolean entradaHoy;
    private final LocalTime horaLlegada;
    private final LocalDateTime horaEncolada;
    private LocalDateTime horaTerminada;

    public TareaPreparacion(Apartamento apartamento, boolean entradaHoy, LocalTime horaLlegada) {
        this.apartamento = apartamento;
        this.entradaHoy = entradaHoy;
        this.horaLlegada = horaLlegada;
        this.horaEncolada = LocalDateTime.now();
    }

    public Apartamento getApartamento() {
        return apartamento;
    }

    public boolean isEntradaHoy() {
        return entradaHoy;
    }

    public LocalTime getHoraLlegada() {
        return horaLlegada;
    }

    public LocalDateTime getHoraEncolada() {
        return horaEncolada;
    }

    public LocalDateTime getHoraTerminada() {
        return horaTerminada;
    }

    /** Marca la tarea como terminada con la hora actual. */
    public void terminar() {
        this.horaTerminada = LocalDateTime.now();
    }

    public boolean estaTerminada() {
        return horaTerminada != null;
    }

    /** Segundos entre que entró a la cola de aseo y quedó terminada. */
    public long segundosDePreparacion() {
        if (horaTerminada == null) {
            return 0;
        }
        return Duration.between(horaEncolada, horaTerminada).getSeconds();
    }

    @Override
    public String toString() {
        return apartamento.getCodigo() + (entradaHoy ? " | entrada hoy" : " | preparación");
    }
}