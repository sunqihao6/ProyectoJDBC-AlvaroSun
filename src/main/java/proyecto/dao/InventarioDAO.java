package proyecto.dao;

import proyecto.clasesTablas.Inventario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class InventarioDAO extends BaseDAO {
    public InventarioDAO(Connection conexion) {
        super(conexion);
    }

    public Inventario buscarPorProducto(int idProducto) throws SQLException {
        String sql = "SELECT id_inventario,id_producto,stock,stock_minimo,actualizado_en "
                + "FROM inventario WHERE id_producto=?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idProducto);
            try (ResultSet resultados = sentencia.executeQuery()) {
                return resultados.next() ? convertirInventario(resultados) : null;
            }
        }
    }

    public boolean actualizarStock(int idProducto, int nuevoStock) throws SQLException {
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        String sql = "UPDATE inventario SET stock=?,actualizado_en=CURRENT_TIMESTAMP "
                + "WHERE id_producto=?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, nuevoStock);
            sentencia.setInt(2, idProducto);
            return sentencia.executeUpdate() > 0;
        }
    }

    private Inventario convertirInventario(ResultSet fila) throws SQLException {
        Timestamp actualizado = fila.getTimestamp("actualizado_en");
        return new Inventario(fila.getInt("id_inventario"), fila.getInt("id_producto"),
                fila.getInt("stock"), fila.getInt("stock_minimo"),
                actualizado == null ? null : actualizado.toLocalDateTime());
    }
}
