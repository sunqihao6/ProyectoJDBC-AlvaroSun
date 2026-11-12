package proyecto.dao;

import proyecto.clasesTablas.Inventario;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Consultas y cambios de existencias de productos. */
public class InventarioDAO extends BaseDAO {
    public InventarioDAO(java.sql.Connection conexion) {
        super(conexion);
    }

    public Inventario buscarPorProducto(long idProducto) throws SQLException {
        String sql = "SELECT id_inventario,id_producto,stock,stock_minimo,actualizado_en "
                + "FROM inventario WHERE id_producto=?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idProducto);
            try (ResultSet resultados = sentencia.executeQuery()) {
                return resultados.next() ? convertirInventario(resultados) : null;
            }
        }
    }

    public boolean actualizarStock(long idProducto, int nuevoStock) throws SQLException {
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        String sql = "UPDATE inventario SET stock=?,actualizado_en=CURRENT_TIMESTAMP "
                + "WHERE id_producto=?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, nuevoStock);
            sentencia.setLong(2, idProducto);
            return sentencia.executeUpdate() > 0;
        }
    }

    public List<Inventario> listarStockBajo() throws SQLException {
        String sql = "SELECT id_inventario,id_producto,stock,stock_minimo,actualizado_en "
                + "FROM inventario WHERE stock<=stock_minimo ORDER BY stock";
        List<Inventario> inventarioBajo = new ArrayList<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultados = sentencia.executeQuery()) {
            while (resultados.next()) {
                inventarioBajo.add(convertirInventario(resultados));
            }
        }
        return inventarioBajo;
    }

    public long crearRegistro(Inventario inventario) throws SQLException {
        String sql = "INSERT INTO inventario (id_producto,stock,stock_minimo) VALUES (?,?,?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setLong(1, inventario.getIdProducto());
            sentencia.setInt(2, inventario.getStock());
            sentencia.setInt(3, inventario.getStockMinimo());
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    long id = claves.getLong(1);
                    inventario.setIdInventario(id);
                    return id;
                }
            }
        }
        throw new SQLException("La base de datos no devolvió el identificador del inventario");
    }

    private Inventario convertirInventario(ResultSet fila) throws SQLException {
        Timestamp actualizado = fila.getTimestamp("actualizado_en");
        return new Inventario(fila.getLong("id_inventario"), fila.getLong("id_producto"),
                fila.getInt("stock"), fila.getInt("stock_minimo"),
                actualizado == null ? null : actualizado.toLocalDateTime());
    }
}
