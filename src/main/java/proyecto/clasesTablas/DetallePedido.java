package proyecto.clasesTablas;

public class DetallePedido {

    private int idDetalle;
    private int idPedido;
    private int idProducto;
    private int cantidadProducto;
    private double precioUnitario;
    public DetallePedido(int idDetalle, int idPedido, int idProducto,
                         int cantidadProducto, double precioUnitario) {
        this.idDetalle = idDetalle;
        this.idPedido = idPedido;
        this.idProducto = idProducto;
        this.cantidadProducto = cantidadProducto;
        this.precioUnitario = precioUnitario;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public int getCantidadProducto() {
        return cantidadProducto;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
}
