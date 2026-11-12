package proyecto.clasesTablas;

/** Representa una entrada, salida, devolución o ajuste de stock. */
public class MovimientoStock {
    private Long idMovimiento;
    private Long idProducto;
    private String tipo;
    private Integer cantidadCambiada;
    private java.time.LocalDateTime fecha;

    /** Constructor vacío para facilitar la creación y carga del objeto. */
    public MovimientoStock() {
    }

    /** Constructor con todos los campos. */
    public MovimientoStock(Long idMovimiento, Long idProducto, String tipo, Integer cantidadCambiada, java.time.LocalDateTime fecha) {
        this.idMovimiento = idMovimiento;
        this.idProducto = idProducto;
        this.tipo = tipo;
        this.cantidadCambiada = cantidadCambiada;
        this.fecha = fecha;
    }

    public Long getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(Long idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getCantidadCambiada() {
        return cantidadCambiada;
    }

    public void setCantidadCambiada(Integer cantidadCambiada) {
        this.cantidadCambiada = cantidadCambiada;
    }

    public java.time.LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(java.time.LocalDateTime fecha) {
        this.fecha = fecha;
    }
}