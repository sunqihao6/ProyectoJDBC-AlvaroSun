package proyecto.dao;

import proyecto.clasesTablas.Categoria;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Operaciones JDBC para categorías y su relación con los productos. */
public class CategoriaDAO extends BaseDAO {
    public CategoriaDAO(java.sql.Connection conexion) {
        super(conexion);
    }

    public long insertar(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categoria (nombre,descripcion) VALUES (?,?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, categoria.getNombre());
            sentencia.setString(2, categoria.getDescripcion());
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    long id = claves.getLong(1);
                    categoria.setIdCategoria(id);
                    return id;
                }
            }
        }
        throw new SQLException("La base de datos no devolvió el identificador de la categoría");
    }

    public List<Categoria> listarTodas() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT id_categoria,nombre,descripcion FROM categoria ORDER BY nombre";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultados = sentencia.executeQuery()) {
            while (resultados.next()) {
                categorias.add(new Categoria(resultados.getLong("id_categoria"),
                        resultados.getString("nombre"), resultados.getString("descripcion")));
            }
        }
        return categorias;
    }

    public boolean asignarProducto(long idProducto, long idCategoria) throws SQLException {
        String sql = "INSERT INTO producto_categoria (id_producto,id_categoria) VALUES (?,?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idProducto);
            sentencia.setLong(2, idCategoria);
            return sentencia.executeUpdate() == 1;
        }
    }
}
