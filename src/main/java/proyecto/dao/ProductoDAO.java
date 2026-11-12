package proyecto.dao;

import proyecto.clasesTablas.Producto;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Operaciones JDBC para productos, incluidas búsquedas por categoría y paginación. */
public class ProductoDAO extends BaseDAO {
    public ProductoDAO(java.sql.Connection conexion) {
        super(conexion);
    }

    public long insertar(Producto producto) throws SQLException {
        String sql = "INSERT INTO producto (nombre,precio,descripcion,activo) VALUES (?,?,?,?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            establecerDatos(sentencia, producto);
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    long id = claves.getLong(1);
                    producto.setIdProducto(id);
                    return id;
                }
            }
        }
        throw new SQLException("La base de datos no devolvió el identificador del producto");
    }

    public Producto buscarPorId(long idProducto) throws SQLException {
        String sql = "SELECT id_producto,nombre,precio,descripcion,activo FROM producto WHERE id_producto=?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idProducto);
            try (ResultSet resultados = sentencia.executeQuery()) {
                return resultados.next() ? convertirProducto(resultados) : null;
            }
        }
    }

    public List<Producto> listarTodos() throws SQLException {
        String sql = "SELECT id_producto,nombre,precio,descripcion,activo FROM producto ORDER BY id_producto";
        return consultarLista(sql);
    }

    public boolean actualizar(Producto producto) throws SQLException {
        String sql = "UPDATE producto SET nombre=?,precio=?,descripcion=?,activo=? WHERE id_producto=?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            establecerDatos(sentencia, producto);
            sentencia.setLong(5, producto.getIdProducto());
            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminar(long idProducto) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(
                "DELETE FROM producto WHERE id_producto=?")) {
            sentencia.setLong(1, idProducto);
            return sentencia.executeUpdate() > 0;
        }
    }

    public List<Producto> buscarPorCategoria(long idCategoria) throws SQLException {
        String sql = "SELECT p.id_producto,p.nombre,p.precio,p.descripcion,p.activo "
                + "FROM producto p JOIN producto_categoria pc ON pc.id_producto=p.id_producto "
                + "WHERE pc.id_categoria=? ORDER BY p.nombre";
        List<Producto> productos = new ArrayList<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idCategoria);
            try (ResultSet resultados = sentencia.executeQuery()) {
                while (resultados.next()) {
                    productos.add(convertirProducto(resultados));
                }
            }
        }
        return productos;
    }

    public List<Producto> listarPaginado(int limite, int desplazamiento) throws SQLException {
        if (limite <= 0 || desplazamiento < 0) {
            throw new IllegalArgumentException("El límite debe ser positivo y el desplazamiento no negativo");
        }
        String sql = "SELECT id_producto,nombre,precio,descripcion,activo FROM producto "
                + "ORDER BY id_producto LIMIT ? OFFSET ?";
        List<Producto> productos = new ArrayList<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, limite);
            sentencia.setInt(2, desplazamiento);
            try (ResultSet resultados = sentencia.executeQuery()) {
                while (resultados.next()) {
                    productos.add(convertirProducto(resultados));
                }
            }
        }
        return productos;
    }

    private List<Producto> consultarLista(String sql) throws SQLException {
        List<Producto> productos = new ArrayList<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultados = sentencia.executeQuery()) {
            while (resultados.next()) {
                productos.add(convertirProducto(resultados));
            }
        }
        return productos;
    }

    private void establecerDatos(PreparedStatement sentencia, Producto producto) throws SQLException {
        sentencia.setString(1, producto.getNombre());
        sentencia.setBigDecimal(2, producto.getPrecio());
        sentencia.setString(3, producto.getDescripcion());
        sentencia.setBoolean(4, producto.isActivo());
    }

    private Producto convertirProducto(ResultSet fila) throws SQLException {
        return new Producto(fila.getLong("id_producto"), fila.getString("nombre"),
                fila.getBigDecimal("precio"), fila.getString("descripcion"),
                fila.getBoolean("activo"));
    }
}
