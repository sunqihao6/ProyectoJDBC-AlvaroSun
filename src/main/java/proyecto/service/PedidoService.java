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

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public Pedido realizarPedido(int idCliente, List<DetallePedido> lineas, String metodoPago)
            throws SQLException {
        return realizarPedido(idCliente, lineas, metodoPago, "PENDIENTE");
    }


    public Pedido realizarPedido(int idCliente, List<DetallePedido> lineas, String metodoPago,
                                 String estadoPedido) throws SQLException {
        validarSolicitud(lineas, metodoPago);
        if (!"PENDIENTE".equals(estadoPedido)
                && !"CANCELADO".equals(estadoPedido) && !"PAGADO".equals(estadoPedido)) {
            throw new IllegalArgumentException("El pedido debe quedar PENDIENTE, CANCELADO o PAGADO");
        }

        boolean autoConfirmacionAnterior = conexion.getAutoCommit();
        conexion.setAutoCommit(false);

        try {
            if (clienteDAO.buscarPorId(idCliente) == null) {
                throw new IllegalArgumentException("No existe el cliente " + idCliente);
            }

            // Comprobar productos y existencias y calcular el total desde precios del catálogo.
            double total = 0.0;
            Set<Integer> productosIncluidos = new HashSet<>();
            for (DetallePedido linea : lineas) {
                if (linea.getIdProducto() <= 0 || linea.getCantidadProducto() <= 0) {
                    throw new IllegalArgumentException(
                            "Cada línea necesita producto y cantidad positiva");
                }

                if (!productosIncluidos.add(linea.getIdProducto())) {
                    throw new IllegalArgumentException(
                            "No repitas un producto; suma sus cantidades en una línea");
                }

                Producto producto = productoDAO.buscarPorId(linea.getIdProducto());
                if (producto == null || !producto.isActivo()) {
                    throw new IllegalArgumentException("El producto no existe o no está activo");
                }

                Inventario inventario = inventarioDAO.buscarPorProducto(linea.getIdProducto());
                if (inventario == null || inventario.getStock() < linea.getCantidadProducto()) {
                    throw new IllegalStateException(
                            "Stock insuficiente para: " + producto.getNombre());
                }

                linea.setPrecioUnitario(producto.getPrecio());
                total += producto.getPrecio() * linea.getCantidadProducto();
            }

            Pedido pedido = new Pedido(0, idCliente, LocalDateTime.now(), estadoPedido, total);
            pedidoDAO.insertar(pedido);

            // Guardar las líneas, descontar existencias y registrar cada salida.
            for (DetallePedido linea : lineas) {
                linea.setIdPedido(pedido.getIdPedido());
                detalleDAO.insertar(linea);

                int stockActual = inventarioDAO.buscarPorProducto(linea.getIdProducto()).getStock();
                inventarioDAO.actualizarStock(linea.getIdProducto(),
                        stockActual - linea.getCantidadProducto());
                movimientoDAO.registrar(new MovimientoStock(0, linea.getIdProducto(), "VENTA",
                        linea.getCantidadProducto(), LocalDateTime.now()));
            }

            Pago pagoPendiente = new Pago(
                    0,
                    pedido.getIdPedido(),
                    metodoPago,
                    "PENDIENTE",
                    total,
                    null);
            pagoDAO.registrar(pagoPendiente);
            conexion.commit();
            return pedido;
        } catch (SQLException | RuntimeException error) {
            conexion.rollback();
            throw error;
        } finally {
            conexion.setAutoCommit(autoConfirmacionAnterior);
        }
    }

    /**
     * Comprueba los datos básicos antes de abrir la transacción.
     */
    private void validarSolicitud(List<DetallePedido> lineas, String metodoPago) {
        if (lineas == null || lineas.size() < 1) {
            throw new IllegalArgumentException(
                    "Cada pedido debe incluir al menos 1 producto");
        }
        if (!"EFECTIVO".equals(metodoPago) && !"TARJETA".equals(metodoPago)) {
            throw new IllegalArgumentException("El método de pago debe ser EFECTIVO o TARJETA");
        }
    }
}
