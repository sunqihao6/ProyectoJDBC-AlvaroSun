package proyecto.clasesTablas;

import java.time.LocalDateTime;

public class Pago {

    private int idPago;
    private int idPedido;
    private String metodoPago;
    private String estado;
    private double importe;
    private LocalDateTime fechaPago;

    public Pago(int idPago, int idPedido, String metodoPago, String estado,
                double importe, LocalDateTime fechaPago) {
        this.idPago = idPago;
        this.idPedido = idPedido;
        this.metodoPago = metodoPago;
        this.estado = estado;
        this.importe = importe;
        this.fechaPago = fechaPago;
    }
    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }


    public int getIdPedido() {
        return idPedido;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public double getImporte() {
        return importe;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
