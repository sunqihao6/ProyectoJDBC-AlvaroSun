package proyecto.dao;

import proyecto.clasesTablas.Pago;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/** Registro y consulta de pagos asociados a pedidos. */
public class PagoDAO extends BaseDAO {
    public PagoDAO(java.sql.Connection conexion) {
        super(conexion);
    }

    public long registrar(Pago pago) throws SQLException {
        String sql = "INSERT INTO pago (id_pedido,metodo_pago,estado,importe,fecha_pago) "
                + "VALUES (?,?,?,?,?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setLong(1, pago.getIdPedido());
            sentencia.setString(2, pago.getMetodoPago());
            sentencia.setString(3, pago.getEstado());
            sentencia.setBigDecimal(4, pago.getImporte());
            if (pago.getFechaPago() == null) {
                sentencia.setNull(5, java.sql.Types.TIMESTAMP);
            } else {
                sentencia.setTimestamp(5, Timestamp.valueOf(pago.getFechaPago()));
            }
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    long id = claves.getLong(1);
                    pago.setIdPago(id);
                    return id;
                }
            }
        }
        throw new SQLException("La base de datos no devolvió el identificador del pago");
    }

    public Pago buscarPorPedido(long idPedido) throws SQLException {
        String sql = "SELECT id_pago,id_pedido,metodo_pago,estado,importe,fecha_pago "
                + "FROM pago WHERE id_pedido=?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idPedido);
            try (ResultSet resultados = sentencia.executeQuery()) {
                if (!resultados.next()) {
                    return null;
                }
                Timestamp fecha = resultados.getTimestamp("fecha_pago");
                return new Pago(resultados.getLong("id_pago"), resultados.getLong("id_pedido"),
                        resultados.getString("metodo_pago"), resultados.getString("estado"),
                        resultados.getBigDecimal("importe"),
                        fecha == null ? null : fecha.toLocalDateTime());
            }
        }
    }

    public boolean actualizarEstado(long idPago, String nuevoEstado) throws SQLException {
        String sql = "UPDATE pago SET estado=? WHERE id_pago=?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, nuevoEstado);
            sentencia.setLong(2, idPago);
            return sentencia.executeUpdate() > 0;
        }
    }
}
