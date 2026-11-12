package proyecto.dao;

import proyecto.clasesTablas.Cliente;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Operaciones JDBC para la tabla cliente. */
public class ClienteDAO extends BaseDAO {
    public ClienteDAO(java.sql.Connection conexion) {
        super(conexion);
    }

    public long insertar(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO cliente (nombre,apellidos,email,telefono) VALUES (?,?,?,?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, cliente.getNombre());
            sentencia.setString(2, cliente.getApellidos());
            sentencia.setString(3, cliente.getEmail());
            sentencia.setString(4, cliente.getTelefono());
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    long id = claves.getLong(1);
                    cliente.setIdCliente(id);
                    return id;
                }
            }
        }
        throw new SQLException("La base de datos no devolvió el identificador del cliente");
    }

    public Cliente buscarPorId(long idCliente) throws SQLException {
        String sql = "SELECT id_cliente,nombre,apellidos,email,telefono FROM cliente WHERE id_cliente=?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idCliente);
            try (ResultSet resultados = sentencia.executeQuery()) {
                return resultados.next() ? convertirCliente(resultados) : null;
            }
        }
    }

    public List<Cliente> listarTodos() throws SQLException {
        String sql = "SELECT id_cliente,nombre,apellidos,email,telefono FROM cliente ORDER BY id_cliente";
        List<Cliente> clientes = new ArrayList<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultados = sentencia.executeQuery()) {
            while (resultados.next()) {
                clientes.add(convertirCliente(resultados));
            }
        }
        return clientes;
    }

    public boolean actualizar(Cliente cliente) throws SQLException {
        String sql = "UPDATE cliente SET nombre=?,apellidos=?,email=?,telefono=? WHERE id_cliente=?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, cliente.getNombre());
            sentencia.setString(2, cliente.getApellidos());
            sentencia.setString(3, cliente.getEmail());
            sentencia.setString(4, cliente.getTelefono());
            sentencia.setLong(5, cliente.getIdCliente());
            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminar(long idCliente) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(
                "DELETE FROM cliente WHERE id_cliente=?")) {
            sentencia.setLong(1, idCliente);
            return sentencia.executeUpdate() > 0;
        }
    }

    private Cliente convertirCliente(ResultSet fila) throws SQLException {
        return new Cliente(fila.getLong("id_cliente"), fila.getString("nombre"),
                fila.getString("apellidos"), fila.getString("email"), fila.getString("telefono"));
    }
}
