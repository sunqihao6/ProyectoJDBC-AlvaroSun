package proyecto.dao;

import proyecto.clasesTablas.MovimientoStock;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Registro y consulta de movimientos de stock. */
public class MovimientoStockDAO extends BaseDAO {
    public MovimientoStockDAO(java.sql.Connection conexion) {
        super(conexion);
    }

    public long registrar(MovimientoStock movimiento) throws SQLException {
        boolean tieneFecha = movimiento.getFecha() != null;
        String sql = tieneFecha
                ? "INSERT INTO movimientos_stock (id_producto,tipo,cantidadCambiada,fecha) VALUES (?,?,?,?)"
                : "INSERT INTO movimientos_stock (id_producto,tipo,cantidadCambiada) VALUES (?,?,?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setLong(1, movimiento.getIdProducto());
            sentencia.setString(2, movimiento.getTipo());
            if (movimiento.getCantidadCambiada() == null) {
                sentencia.setNull(3, java.sql.Types.INTEGER);
            } else {
                sentencia.setInt(3, movimiento.getCantidadCambiada());
            }
            if (tieneFecha) {
                sentencia.setTimestamp(4, Timestamp.valueOf(movimiento.getFecha()));
            }
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    long id = claves.getLong(1);
                    movimiento.setIdMovimiento(id);
                    return id;
                }
            }
        }
        throw new SQLException("La base de datos no devolvió el identificador del movimiento");
    }

    public List<MovimientoStock> listarPorProducto(long idProducto) throws SQLException {
        String sql = "SELECT id_movimiento,id_producto,tipo,cantidadCambiada,fecha "
                + "FROM movimientos_stock WHERE id_producto=? ORDER BY fecha,id_movimiento";
        List<MovimientoStock> movimientos = new ArrayList<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idProducto);
            try (ResultSet resultados = sentencia.executeQuery()) {
                while (resultados.next()) {
                    Timestamp fecha = resultados.getTimestamp("fecha");
                    int cantidad = resultados.getInt("cantidadCambiada");
                    Integer cantidadNula = resultados.wasNull() ? null : cantidad;
                    movimientos.add(new MovimientoStock(resultados.getLong("id_movimiento"),
                            resultados.getLong("id_producto"), resultados.getString("tipo"),
                            cantidadNula, fecha == null ? null : fecha.toLocalDateTime()));
                }
            }
        }
        return movimientos;
    }
}
