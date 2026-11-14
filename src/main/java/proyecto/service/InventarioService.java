package proyecto.service;

import proyecto.clasesTablas.Inventario;
import proyecto.clasesTablas.MovimientoStock;
import proyecto.dao.InventarioDAO;
import proyecto.dao.MovimientoStockDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/** Coordina los cambios de existencias con su registro de movimientos. */
public class InventarioService {
    private final Connection conexion;
    private final InventarioDAO inventarioDAO;
    private final MovimientoStockDAO movimientoDAO;

    public InventarioService(Connection conexion) {
        this.conexion = conexion;
        inventarioDAO = new InventarioDAO(conexion);
        movimientoDAO = new MovimientoStockDAO(conexion);
    }

    public Inventario consultarExistencias(long idProducto) throws SQLException {
        return inventarioDAO.buscarPorProducto(idProducto);
    }

    public List<Inventario> consultarStockBajo() throws SQLException {
        return inventarioDAO.listarStockBajo();
    }

    public void reponerStock(long idProducto, int cantidad) throws SQLException {
        cambiarStock(idProducto, cantidad, "COMPRA");
    }

    public void retirarStock(long idProducto, int cantidad) throws SQLException {
        cambiarStock(idProducto, -cantidad, "VENTA");
    }

    private void cambiarStock(long idProducto, int cambio, String tipoMovimiento)
            throws SQLException {
        if (cambio == 0 || (tipoMovimiento.equals("COMPRA") && cambio < 0)
                || (tipoMovimiento.equals("VENTA") && cambio > 0)) {
            throw new IllegalArgumentException("La cantidad del movimiento no es válida");
        }

        boolean autoConfirmacionAnterior = conexion.getAutoCommit();
        conexion.setAutoCommit(false);
        try {
            Inventario inventario = inventarioDAO.buscarPorProducto(idProducto);
            if (inventario == null) {
                throw new IllegalArgumentException("No existe inventario para el producto " + idProducto);
            }

            int stockNuevo = inventario.getStock() + cambio;
            if (stockNuevo < 0) {
                throw new IllegalStateException("No hay existencias suficientes");
            }
            inventarioDAO.actualizarStock(idProducto, stockNuevo);
            movimientoDAO.registrar(new MovimientoStock(null, idProducto, tipoMovimiento,
                    Math.abs(cambio), LocalDateTime.now()));
            conexion.commit();
        } catch (SQLException | RuntimeException error) {
            conexion.rollback();
            throw error;
        } finally {
            conexion.setAutoCommit(autoConfirmacionAnterior);
        }
    }
}
