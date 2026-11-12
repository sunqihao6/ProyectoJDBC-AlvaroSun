package proyecto.clasesTablas;

/** Representa un pedido realizado por un cliente. */
public class Pedido {
    private Long idPedido;
    private Long idCliente;
    private java.time.LocalDateTime fecha;
    private String estado;
    private java.math.BigDecimal total;

    /** Constructor vacío para facilitar la creación y carga del objeto. */
    public Pedido() {
    }

    /** Constructor con todos los campos. */
    public Pedido(Long idPedido, Long idCliente, java.time.LocalDateTime fecha, String estado, java.math.BigDecimal total) {
        this.idPedido = idPedido;
        this.idCliente = idCliente;
        this.fecha = fecha;
        this.estado = estado;
        this.total = total;
    }

    public Long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Long idPedido) {
        this.idPedido = idPedido;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public java.time.LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(java.time.LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public java.math.BigDecimal getTotal() {
        return total;
    }

    public void setTotal(java.math.BigDecimal total) {
        this.total = total;
    }
}