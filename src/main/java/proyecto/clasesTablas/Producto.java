package proyecto.clasesTablas;

/** Representa un componente o periférico del catálogo. */
public class Producto {
    private Long idProducto;
    private String nombre;
    private java.math.BigDecimal precio;
    private String descripcion;
    private boolean activo;

    /** Constructor vacío para facilitar la creación y carga del objeto. */
    public Producto() {
    }

    /** Constructor con todos los campos. */
    public Producto(Long idProducto, String nombre, java.math.BigDecimal precio, String descripcion, boolean activo) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.activo = activo;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public java.math.BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(java.math.BigDecimal precio) {
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}