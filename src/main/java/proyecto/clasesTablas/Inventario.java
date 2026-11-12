package proyecto.clasesTablas;

/** Representa las existencias y el mínimo de stock de un producto. */
public class Inventario {
    private Long idInventario;
    private Long idProducto;
    private Integer stock;
    private Integer stockMinimo;
    private java.time.LocalDateTime actualizadoEn;

    /** Constructor vacío para facilitar la creación y carga del objeto. */
    public Inventario() {
    }

    /** Constructor con todos los campos. */
    public Inventario(Long idInventario, Long idProducto, Integer stock, Integer stockMinimo, java.time.LocalDateTime actualizadoEn) {
        this.idInventario = idInventario;
        this.idProducto = idProducto;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.actualizadoEn = actualizadoEn;
    }

    public Long getIdInventario() {
        return idInventario;
    }

    public void setIdInventario(Long idInventario) {
        this.idInventario = idInventario;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public java.time.LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(java.time.LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }
}