package proyecto.clasesTablas;

/** Representa la relación entre un producto y una categoría. */
public class ProductoCategoria {
    private Long idProducto;
    private Long idCategoria;

    /** Constructor vacío para facilitar la creación y carga del objeto. */
    public ProductoCategoria() {
    }

    /** Constructor con todos los campos. */
    public ProductoCategoria(Long idProducto, Long idCategoria) {
        this.idProducto = idProducto;
        this.idCategoria = idCategoria;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public Long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Long idCategoria) {
        this.idCategoria = idCategoria;
    }
}