package proyecto.clasesTablas;

import java.time.LocalDateTime;

public class MovimientoStock {

    private int idMovimiento;
    private int idProducto;
    private String tipo;
    private int cantidadCambiada;
    private LocalDateTime fecha;
    public MovimientoStock(int idMovimiento, int idProducto, String tipo,
                           int cantidadCambiada, LocalDateTime fecha) {
        this.idMovimiento = idMovimiento;
        this.idProducto = idProducto;
        this.tipo = tipo;
        this.cantidadCambiada = cantidadCambiada;
        this.fecha = fecha;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }


    public int getIdProducto() {
        return idProducto;
    }


    public String getTipo() {
        return tipo;
    }

    public int getCantidadCambiada() {
        return cantidadCambiada;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
