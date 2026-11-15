package proyecto.clasesTablas;

public class Producto {

    private int idProducto;
    private String nombre;
    private double precio;
    private String descripcion;
    private boolean activo;
    public Producto(int idProducto, String nombre, double precio,
                    String descripcion, boolean activo) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.activo = activo;
    }


    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }


    public boolean isActivo() {
        return activo;
    }

}
