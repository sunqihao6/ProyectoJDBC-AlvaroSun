package proyecto.dao;

import proyecto.clasesTablas.MovimientoStock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class MovimientoStockDAO extends BaseDAO {
    public MovimientoStockDAO(Connection conexion) {
        super(conexion);
    }

    public int registrar(MovimientoStock movimiento) throws SQLException {

        String sql =
                "INSERT INTO movimientos_stock (id_producto,tipo,cantidadCambiada,fecha) VALUES (?,?,?,?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setInt(1, movimiento.getIdProducto());
            sentencia.setString(2, movimiento.getTipo());
            sentencia.setInt(3, movimiento.getCantidadCambiada());
            sentencia.setTimestamp(4, Timestamp.valueOf(movimiento.getFecha()));

            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    int id = claves.getInt(1);
                    movimiento.setIdMovimiento(id);
                    return id;
                }
            }
        }
        throw new SQLException("La base de datos no devolvió el identificador del movimiento");
    }
}
