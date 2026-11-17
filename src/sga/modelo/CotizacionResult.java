package modelo;

public class CotizacionResult {
    private final DetalleNoche[] desgloseNoches;
    private final double valorTotal;
    private final double anticipoRequerido;
    private final int totalOcupantes;
    private final int ocupantesQuePagan;

    public CotizacionResult(DetalleNoche[] desgloseNoches, double valorTotal,
            double anticipoRequerido, int totalOcupantes, int ocupantesQuePagan) {
        this.desgloseNoches = desgloseNoches;
        this.valorTotal = valorTotal;
        this.anticipoRequerido = anticipoRequerido;
        this.totalOcupantes = totalOcupantes;
        this.ocupantesQuePagan = ocupantesQuePagan;
    }

    public DetalleNoche[] getDesgloseNoches() {
        return desgloseNoches;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public double getAnticipoRequerido() {
        return anticipoRequerido;
    }

    public int getTotalOcupantes() {
        return totalOcupantes;
    }

    public int getOcupantesQuePagan() {
        return ocupantesQuePagan;
    }
}
