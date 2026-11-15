package proyecto.dao;

import proyecto.clasesTablas.Producto;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO extends BaseDAO {
    public ProductoDAO(Connection conexion) {
        super(conexion);
    }

    public int insertar(Producto producto) throws SQLException {

        String sql = "INSERT INTO producto (nombre,precio,descripcion,activo) VALUES (?,?,?,?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            sentencia.setString(1, producto.getNombre());
            sentencia.setDouble(2, producto.getPrecio());
            sentencia.setString(3, producto.getDescripcion());
            sentencia.setBoolean(4, producto.isActivo());
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    int id = claves.getInt(1);
                    producto.setIdProducto(id);
                    return id;
                }
            }
        }
        throw new SQLException("La base de datos no devolvió el identificador del producto");
    }

    public Producto buscarPorId(int idProducto) throws SQLException {
        String sql = "SELECT id_producto,nombre,precio,descripcion,activo FROM producto WHERE id_producto=?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idProducto);
            try (ResultSet resultados = sentencia.executeQuery()) {
                return resultados.next() ? convertirProducto(resultados) : null;
            }
        }
    }


    private Producto convertirProducto(ResultSet fila) throws SQLException {
        return new Producto(fila.getInt("id_producto"), fila.getString("nombre"),
                fila.getDouble("precio"), fila.getString("descripcion"),
                fila.getBoolean("activo"));
    }
}
