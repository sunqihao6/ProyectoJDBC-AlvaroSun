package proyecto.dao;

import proyecto.clasesTablas.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO extends BaseDAO {
    public ClienteDAO(Connection conexion) {
        super(conexion);
    }

    public Cliente buscarPorId(int idCliente) throws SQLException {
        String sql = "SELECT id_cliente,nombre,apellidos,email,telefono FROM cliente WHERE id_cliente=?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idCliente);
            try (ResultSet resultados = sentencia.executeQuery()) {
                while(resultados.next()){
                    return new Cliente(resultados.getInt("id_cliente"), resultados.getString("nombre"), resultados.getString("apellidos"), resultados.getString("email"), resultados.getString("telefono"));
                }
                return null;
            }
        }
    }
}
