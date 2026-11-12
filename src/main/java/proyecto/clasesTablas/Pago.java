package proyecto.clasesTablas;

/** Representa el pago simulado asociado a un pedido. */
public class Pago {
    private Long idPago;
    private Long idPedido;
    private String metodoPago;
    private String estado;
    private java.math.BigDecimal importe;
    private java.time.LocalDateTime fechaPago;

    /** Constructor vacío para facilitar la creación y carga del objeto. */
    public Pago() {
    }

    /** Constructor con todos los campos. */
    public Pago(Long idPago, Long idPedido, String metodoPago, String estado, java.math.BigDecimal importe, java.time.LocalDateTime fechaPago) {
        this.idPago = idPago;
        this.idPedido = idPedido;
        this.metodoPago = metodoPago;
        this.estado = estado;
        this.importe = importe;
        this.fechaPago = fechaPago;
    }

    public Long getIdPago() {
        return idPago;
    }

    public void setIdPago(Long idPago) {
        this.idPago = idPago;
    }

    public Long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Long idPedido) {
        this.idPedido = idPedido;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public java.math.BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(java.math.BigDecimal importe) {
        this.importe = importe;
    }

    public java.time.LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(java.time.LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }
}