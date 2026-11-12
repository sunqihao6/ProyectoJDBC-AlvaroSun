package proyecto.dao;

import proyecto.clasesTablas.Pedido;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Operaciones JDBC de cabecera de pedido. Las líneas se gestionan en DetallePedidoDAO. */
public class PedidoDAO extends BaseDAO {
    public PedidoDAO(java.sql.Connection conexion) {
        super(conexion);
    }

    public long insertar(Pedido pedido) throws SQLException {
        boolean tieneFecha = pedido.getFecha() != null;
        String sql = tieneFecha
                ? "INSERT INTO pedido (id_cliente,fecha,estado,total) VALUES (?,?,?,?)"
                : "INSERT INTO pedido (id_cliente,estado,total) VALUES (?,?,?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setLong(1, pedido.getIdCliente());
            if (tieneFecha) {
                sentencia.setTimestamp(2, Timestamp.valueOf(pedido.getFecha()));
                sentencia.setString(3, pedido.getEstado());
                sentencia.setBigDecimal(4, pedido.getTotal());
            } else {
                sentencia.setString(2, pedido.getEstado());
                sentencia.setBigDecimal(3, pedido.getTotal());
            }
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    long id = claves.getLong(1);
                    pedido.setIdPedido(id);
                    return id;
                }
            }
        }
        throw new SQLException("La base de datos no devolvió el identificador del pedido");
    }

    public Pedido buscarPorId(long idPedido) throws SQLException {
        String sql = "SELECT id_pedido,id_cliente,fecha,estado,total FROM pedido WHERE id_pedido=?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idPedido);
            try (ResultSet resultados = sentencia.executeQuery()) {
                return resultados.next() ? convertirPedido(resultados) : null;
            }
        }
    }

    public List<Pedido> listarPorCliente(long idCliente) throws SQLException {
        String sql = "SELECT id_pedido,id_cliente,fecha,estado,total FROM pedido "
                + "WHERE id_cliente=? ORDER BY fecha DESC";
        List<Pedido> pedidos = new ArrayList<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idCliente);
            try (ResultSet resultados = sentencia.executeQuery()) {
                while (resultados.next()) {
                    pedidos.add(convertirPedido(resultados));
                }
            }
        }
        return pedidos;
    }

    public boolean actualizarEstado(long idPedido, String nuevoEstado) throws SQLException {
        String sql = "UPDATE pedido SET estado=? WHERE id_pedido=?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, nuevoEstado);
            sentencia.setLong(2, idPedido);
            return sentencia.executeUpdate() > 0;
        }
    }

    private Pedido convertirPedido(ResultSet fila) throws SQLException {
        Timestamp fecha = fila.getTimestamp("fecha");
        return new Pedido(fila.getLong("id_pedido"), fila.getLong("id_cliente"),
                fecha == null ? null : fecha.toLocalDateTime(), fila.getString("estado"),
                fila.getBigDecimal("total"));
    }
}
