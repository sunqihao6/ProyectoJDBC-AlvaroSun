package proyecto.dao;

import proyecto.clasesTablas.Pedido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO extends BaseDAO {
    public PedidoDAO(Connection conexion) {
        super(conexion);
    }

    public int insertar(Pedido pedido) throws SQLException {

        String sql = "INSERT INTO pedido (id_cliente,fecha,estado,total) VALUES (?,?,?,?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setInt(1, pedido.getIdCliente());

            sentencia.setTimestamp(2, Timestamp.valueOf(pedido.getFecha()));
            sentencia.setString(3, pedido.getEstado());
            sentencia.setDouble(4, pedido.getTotal());

            sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    int id = claves.getInt(1);
                    pedido.setIdPedido(id);
                    return id;
                }
            }
        }
        throw new SQLException("La base de datos no devolvió el identificador del pedido");
    }
}
