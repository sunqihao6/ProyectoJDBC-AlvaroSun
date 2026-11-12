package proyecto.dao;

import proyecto.clasesTablas.Direccion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Operaciones JDBC para direcciones y su asociación con clientes. */
public class DireccionDAO extends BaseDAO {
    public DireccionDAO(java.sql.Connection conexion) {
        super(conexion);
    }

    public long insertar(Direccion direccion) throws SQLException {
        String sql = "INSERT INTO direccion (calle,ciudad,codigo_postal,pais) VALUES (?,?,?,?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, direccion.getCalle());
            sentencia.setString(2, direccion.getCiudad());
            sentencia.setString(3, direccion.getCodigoPostal());
            sentencia.setString(4, direccion.getPais());
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    long id = claves.getLong(1);
                    direccion.setIdDireccion(id);
                    return id;
                }
            }
        }
        throw new SQLException("La base de datos no devolvió el identificador de la dirección");
    }

    public boolean asociarACliente(long idCliente, long idDireccion) throws SQLException {
        String sql = "INSERT INTO cliente_direccion (id_cliente,id_direccion) VALUES (?,?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idCliente);
            sentencia.setLong(2, idDireccion);
            return sentencia.executeUpdate() == 1;
        }
    }

    public List<Direccion> listarPorCliente(long idCliente) throws SQLException {
        String sql = "SELECT d.id_direccion,d.calle,d.ciudad,d.codigo_postal,d.pais "
                + "FROM direccion d JOIN cliente_direccion cd ON cd.id_direccion=d.id_direccion "
                + "WHERE cd.id_cliente=? ORDER BY d.id_direccion";
        List<Direccion> direcciones = new ArrayList<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idCliente);
            try (ResultSet resultados = sentencia.executeQuery()) {
                while (resultados.next()) {
                    direcciones.add(new Direccion(resultados.getLong("id_direccion"),
                            resultados.getString("calle"), resultados.getString("ciudad"),
                            resultados.getString("codigo_postal"), resultados.getString("pais")));
                }
            }
        }
        return direcciones;
    }
}
