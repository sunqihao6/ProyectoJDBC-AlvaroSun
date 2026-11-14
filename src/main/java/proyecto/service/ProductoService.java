package proyecto.service;

import proyecto.clasesTablas.Producto;
import proyecto.dao.ProductoDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Reglas de validación y consultas del catálogo. */
public class ProductoService {
    private final ProductoDAO productoDAO;

    public ProductoService(Connection conexion) {
        productoDAO = new ProductoDAO(conexion);
    }

    public long registrarProducto(Producto producto) throws SQLException {
        validarProducto(producto);
        return productoDAO.insertar(producto);
    }

    public boolean modificarProducto(Producto producto) throws SQLException {
        validarProducto(producto);
        if (producto.getIdProducto() == null) {
            throw new IllegalArgumentException("El producto debe tener identificador");
        }
        return productoDAO.actualizar(producto);
    }

    public Producto consultarProducto(long idProducto) throws SQLException {
        return productoDAO.buscarPorId(idProducto);
    }

    public List<Producto> consultarCatalogo() throws SQLException {
        return productoDAO.listarTodos();
    }

    public List<Producto> consultarPorCategoria(long idCategoria) throws SQLException {
        return productoDAO.buscarPorCategoria(idCategoria);
    }

    private void validarProducto(Producto producto) {
        if (producto == null || producto.getNombre() == null || producto.getNombre().isBlank()
                || producto.getPrecio() == null || producto.getPrecio().signum() < 0) {
            throw new IllegalArgumentException("El producto necesita nombre y precio no negativo");
        }
    }
}
