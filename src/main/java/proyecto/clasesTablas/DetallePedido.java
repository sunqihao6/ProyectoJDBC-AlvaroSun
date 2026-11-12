package proyecto.clasesTablas;

/** Representa un producto, cantidad y precio unitario dentro de un pedido. */
public class DetallePedido {
    private Long idDetalle;
    private Long idPedido;
    private Long idProducto;
    private Integer cantidadProducto;
    private java.math.BigDecimal precioUnitario;

    /** Constructor vacío para facilitar la creación y carga del objeto. */
    public DetallePedido() {
    }

    /** Constructor con todos los campos. */
    public DetallePedido(Long idDetalle, Long idPedido, Long idProducto, Integer cantidadProducto, java.math.BigDecimal precioUnitario) {
        this.idDetalle = idDetalle;
        this.idPedido = idPedido;
        this.idProducto = idProducto;
        this.cantidadProducto = cantidadProducto;
        this.precioUnitario = precioUnitario;
    }

    public Long getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Long idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Long idPedido) {
        this.idPedido = idPedido;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getCantidadProducto() {
        return cantidadProducto;
    }

    public void setCantidadProducto(Integer cantidadProducto) {
        this.cantidadProducto = cantidadProducto;
    }

    public java.math.BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(java.math.BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
}