package proyecto.dao;

import proyecto.clasesTablas.DetallePedido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DetallePedidoDAO extends BaseDAO {
    public DetallePedidoDAO(Connection conexion) {
        super(conexion);
    }

    public int insertar(DetallePedido detalle) throws SQLException {
        String sql = "INSERT INTO detalle_pedido "
                + "(id_pedido,id_producto,cantidadProducto,precio_unitario) VALUES (?,?,?,?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setInt(1, detalle.getIdPedido());
            sentencia.setInt(2, detalle.getIdProducto());
            sentencia.setInt(3, detalle.getCantidadProducto());
            sentencia.setDouble(4, detalle.getPrecioUnitario());
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    int id = claves.getInt(1);
                    detalle.setIdDetalle(id);
                    return id;
                }
            }
        }
        throw new SQLException("La base de datos no devolvió el identificador de la línea");
    }

    public List<DetallePedido> listarPorPedido(int idPedido) throws SQLException {
        String sql = "SELECT id_detalle,id_pedido,id_producto,cantidadProducto,precio_unitario "
                + "FROM detalle_pedido WHERE id_pedido=? ORDER BY id_detalle";

        List<DetallePedido> detalles = new ArrayList<>();

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPedido);

            try (ResultSet resultados = sentencia.executeQuery()) {
                while (resultados.next()) {
                    detalles.add(new DetallePedido(resultados.getInt("id_detalle"),
                            resultados.getInt("id_pedido"), resultados.getInt("id_producto"),
                            resultados.getInt("cantidadProducto"),
                            resultados.getDouble("precio_unitario")));
                }
            }
        }
        return detalles;
    }
}
