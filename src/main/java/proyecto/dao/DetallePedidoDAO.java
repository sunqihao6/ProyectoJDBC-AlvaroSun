package proyecto.dao;

import proyecto.clasesTablas.DetallePedido;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Operaciones JDBC para cada línea de producto incluida en un pedido. */
public class DetallePedidoDAO extends BaseDAO {
    public DetallePedidoDAO(java.sql.Connection conexion) {
        super(conexion);
    }

    public long insertar(DetallePedido detalle) throws SQLException {
        String sql = "INSERT INTO detalle_pedido "
                + "(id_pedido,id_producto,cantidadProducto,precio_unitario) VALUES (?,?,?,?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setLong(1, detalle.getIdPedido());
            sentencia.setLong(2, detalle.getIdProducto());
            sentencia.setInt(3, detalle.getCantidadProducto());
            sentencia.setBigDecimal(4, detalle.getPrecioUnitario());
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    long id = claves.getLong(1);
                    detalle.setIdDetalle(id);
                    return id;
                }
            }
        }
        throw new SQLException("La base de datos no devolvió el identificador de la línea");
    }

    public List<DetallePedido> listarPorPedido(long idPedido) throws SQLException {
        String sql = "SELECT id_detalle,id_pedido,id_producto,cantidadProducto,precio_unitario "
                + "FROM detalle_pedido WHERE id_pedido=? ORDER BY id_detalle";
        List<DetallePedido> detalles = new ArrayList<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idPedido);
            try (ResultSet resultados = sentencia.executeQuery()) {
                while (resultados.next()) {
                    detalles.add(new DetallePedido(resultados.getLong("id_detalle"),
                            resultados.getLong("id_pedido"), resultados.getLong("id_producto"),
                            resultados.getInt("cantidadProducto"),
                            resultados.getBigDecimal("precio_unitario")));
                }
            }
        }
        return detalles;
    }
}
