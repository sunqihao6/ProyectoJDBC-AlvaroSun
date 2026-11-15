package proyecto.dao;

import java.sql.Connection;

public abstract class BaseDAO {
    protected final Connection conexion;

    protected BaseDAO(Connection conexion) {
        if (conexion == null) {
            throw new IllegalArgumentException("La conexión no puede ser nula");
        }
        this.conexion = conexion;
    }
}
