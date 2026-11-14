package proyecto.service;

import proyecto.clasesTablas.DetallePedido;
import proyecto.clasesTablas.Inventario;
import proyecto.clasesTablas.MovimientoStock;
import proyecto.clasesTablas.Pago;
import proyecto.clasesTablas.Pedido;
import proyecto.clasesTablas.Producto;
import proyecto.dao.ClienteDAO;
import proyecto.dao.DetallePedidoDAO;
import proyecto.dao.InventarioDAO;
import proyecto.dao.MovimientoStockDAO;
import proyecto.dao.PagoDAO;
import proyecto.dao.PedidoDAO;
import proyecto.dao.ProductoDAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Coordina pedidos, líneas, inventario, movimientos y pago en una transacción. */
public class PedidoService {
    private final Connection conexion;
    private final ClienteDAO clienteDAO;
    private final ProductoDAO productoDAO;
    private final PedidoDAO pedidoDAO;
    private final DetallePedidoDAO detalleDAO;
    private final InventarioDAO inventarioDAO;
    private final MovimientoStockDAO movimientoDAO;
    private final PagoDAO pagoDAO;

    public PedidoService(Connection conexion) {
        this.conexion = conexion;
        clienteDAO = new ClienteDAO(conexion);
        productoDAO = new ProductoDAO(conexion);
        pedidoDAO = new PedidoDAO(conexion);
        detalleDAO = new DetallePedidoDAO(conexion);
        inventarioDAO = new InventarioDAO(conexion);
        movimientoDAO = new MovimientoStockDAO(conexion);
        pagoDAO = new PagoDAO(conexion);
    }

    /** Crea un pedido completo y deshace todos los cambios si falla una operación. */
    public Pedido realizarPedido(long idCliente, List<DetallePedido> lineas, String metodoPago)
            throws SQLException {
        validarSolicitud(lineas, metodoPago);
        boolean autoConfirmacionAnterior = conexion.getAutoCommit();
        conexion.setAutoCommit(false);
        try {
            if (clienteDAO.buscarPorId(idCliente) == null) {
                throw new IllegalArgumentException("No existe el cliente " + idCliente);
            }

            // Comprobar productos y existencias y calcular el total desde precios del catálogo.
            BigDecimal total = BigDecimal.ZERO;
            Set<Long> productosIncluidos = new HashSet<>();
            for (DetallePedido linea : lineas) {
                if (linea.getIdProducto() == null || linea.getCantidadProducto() == null
                        || linea.getCantidadProducto() <= 0) {
                    throw new IllegalArgumentException("Cada línea necesita producto y cantidad positiva");
                }
                if (!productosIncluidos.add(linea.getIdProducto())) {
                    throw new IllegalArgumentException("No repitas un producto; suma sus cantidades en una línea");
                }

                Producto producto = productoDAO.buscarPorId(linea.getIdProducto());
                if (producto == null || !producto.isActivo()) {
                    throw new IllegalArgumentException("El producto no existe o no está activo");
                }
                Inventario inventario = inventarioDAO.buscarPorProducto(linea.getIdProducto());
                if (inventario == null || inventario.getStock() < linea.getCantidadProducto()) {
                    throw new IllegalStateException("Stock insuficiente para: " + producto.getNombre());
                }

                linea.setPrecioUnitario(producto.getPrecio());
                total = total.add(producto.getPrecio()
                        .multiply(BigDecimal.valueOf(linea.getCantidadProducto())));
            }

            Pedido pedido = new Pedido(null, idCliente, LocalDateTime.now(), "COMPLETADO", total);
            pedidoDAO.insertar(pedido);

            // Guardar las líneas, descontar existencias y registrar cada salida.
            for (DetallePedido linea : lineas) {
                linea.setIdPedido(pedido.getIdPedido());
                detalleDAO.insertar(linea);

                int stockActual = inventarioDAO.buscarPorProducto(linea.getIdProducto()).getStock();
                inventarioDAO.actualizarStock(linea.getIdProducto(),
                        stockActual - linea.getCantidadProducto());
                movimientoDAO.registrar(new MovimientoStock(null, linea.getIdProducto(), "VENTA",
                        linea.getCantidadProducto(), LocalDateTime.now()));
            }

            pagoDAO.registrar(new Pago(null, pedido.getIdPedido(), metodoPago, "COMPLETADO",
                    total, LocalDateTime.now()));
            conexion.commit();
            return pedido;
        } catch (SQLException | RuntimeException error) {
            conexion.rollback();
            throw error;
        } finally {
            conexion.setAutoCommit(autoConfirmacionAnterior);
        }
    }

    public Pedido consultarPedido(long idPedido) throws SQLException {
        return pedidoDAO.buscarPorId(idPedido);
    }

    public List<DetallePedido> consultarLineas(long idPedido) throws SQLException {
        return detalleDAO.listarPorPedido(idPedido);
    }

    private void validarSolicitud(List<DetallePedido> lineas, String metodoPago) {
        if (lineas == null || lineas.isEmpty()) {
            throw new IllegalArgumentException("El pedido debe incluir al menos un producto");
        }
        if (!"EFECTIVO".equals(metodoPago) && !"TARJETA".equals(metodoPago)) {
            throw new IllegalArgumentException("El método de pago debe ser EFECTIVO o TARJETA");
        }
    }
}
