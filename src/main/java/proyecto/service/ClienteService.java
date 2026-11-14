package proyecto.service;

import proyecto.clasesTablas.Cliente;
import proyecto.dao.ClienteDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Reglas sencillas de registro y consulta de clientes. */
public class ClienteService {
    private final ClienteDAO clienteDAO;

    public ClienteService(Connection conexion) {
        clienteDAO = new ClienteDAO(conexion);
    }

    public long registrarCliente(Cliente cliente) throws SQLException {
        if (cliente == null || cliente.getNombre() == null || cliente.getNombre().isBlank()
                || cliente.getApellidos() == null || cliente.getApellidos().isBlank()
                || cliente.getEmail() == null || cliente.getEmail().isBlank()) {
            throw new IllegalArgumentException("Nombre, apellidos y correo son obligatorios");
        }
        return clienteDAO.insertar(cliente);
    }

    public Cliente consultarCliente(long idCliente) throws SQLException {
        return clienteDAO.buscarPorId(idCliente);
    }

    public List<Cliente> listarClientes() throws SQLException {
        return clienteDAO.listarTodos();
    }

    public boolean actualizarCliente(Cliente cliente) throws SQLException {
        if (cliente == null || cliente.getIdCliente() == null) {
            throw new IllegalArgumentException("El cliente debe tener identificador");
        }
        return clienteDAO.actualizar(cliente);
    }
}
