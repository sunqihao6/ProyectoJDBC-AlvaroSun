package proyecto.dao;

import java.sql.Connection;

/** Comparte una conexión entre DAO; no la cierra para permitir transacciones coordinadas. */
public abstract class BaseDAO {
    protected final Connection conexion;

    protected BaseDAO(Connection conexion) {
        if (conexion == null) {
            throw new IllegalArgumentException("La conexión no puede ser nula");
        }
        this.conexion = conexion;
    }
}
