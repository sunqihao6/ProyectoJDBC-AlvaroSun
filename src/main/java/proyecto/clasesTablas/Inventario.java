package proyecto.clasesTablas;

import java.time.LocalDateTime;

public class Inventario {

    private int idInventario;
    private int idProducto;
    private int stock;
    private int stockMinimo;
    private LocalDateTime actualizadoEn;

    public Inventario(int idInventario, int idProducto, int stock,
                      int stockMinimo, LocalDateTime actualizadoEn) {
        this.idInventario = idInventario;
        this.idProducto = idProducto;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.actualizadoEn = actualizadoEn;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
