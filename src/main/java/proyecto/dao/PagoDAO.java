package proyecto.dao;

import proyecto.clasesTablas.Pago;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;

public class PagoDAO extends BaseDAO {
    public PagoDAO(Connection conexion) {
        super(conexion);
    }

    public int registrar(Pago pago) throws SQLException {
        String sql = "INSERT INTO pago (id_pedido,metodo_pago,estado,importe,fecha_pago) "
                + "VALUES (?,?,?,?,?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setInt(1, pago.getIdPedido());
            sentencia.setString(2, pago.getMetodoPago());
            sentencia.setString(3, pago.getEstado());
            sentencia.setDouble(4, pago.getImporte());
            if(pago.getFechaPago()!=null) {
                sentencia.setTimestamp(5, Timestamp.valueOf(pago.getFechaPago()));
            }else{
                sentencia.setTimestamp(5,null);
            }
            sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    int id = claves.getInt(1);
                    pago.setIdPago(id);
                    return id;
                }
            }
        }
        throw new SQLException("La base de datos no devolvió el identificador del pago");
    }
}
