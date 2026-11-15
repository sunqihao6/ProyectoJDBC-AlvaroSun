package proyecto.database;

import proyecto.dto.CategoriaDTO;
import proyecto.dto.ClienteDTO;
import proyecto.dto.MovimientoDTO;
import proyecto.dto.PedidoDTO;
import proyecto.dto.ProductoDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ProcedimientosH2 {
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd:MM:yyyy HH:mm:ss");

    private ProcedimientosH2() {
    }

    //Recupera cada cliente y agrega a su DTO las direcciones relacionadas
    public static List<ClienteDTO> clientes(Connection conexion) throws SQLException {
        Map<Integer, ClienteDTO> clientes = new LinkedHashMap<>();
        String sql = """
                SELECT cl.id_cliente, cl.nombre, cl.apellidos, cl.email,
                       d.calle, d.ciudad, d.codigo_postal, d.pais
                FROM cliente cl
                LEFT JOIN cliente_direccion cd ON cd.id_cliente = cl.id_cliente
                LEFT JOIN direccion d ON d.id_direccion = cd.id_direccion
                ORDER BY cl.id_cliente, d.id_direccion
                """;

        try (Statement consulta = conexion.createStatement();
             ResultSet filas = consulta.executeQuery(sql)) {
            while (filas.next()) {
                int idCliente = filas.getInt("id_cliente");
                ClienteDTO cliente = clientes.get(idCliente);

                if (cliente == null) {
                    String nombreCompleto = filas.getString("nombre")
                            + " " + filas.getString("apellidos");
                    cliente = new ClienteDTO(
                            idCliente, nombreCompleto, filas.getString("email"));
                    clientes.put(idCliente, cliente);
                }

                String calle = filas.getString("calle");
                if (calle != null) {
                    String direccion = construirDireccion(filas, calle);
                    cliente.agregarDireccion(direccion);
                }
            }
        }

        return new ArrayList<>(clientes.values());
    }

    // Recupera productos y agrupa sus DTOs por categoría
    public static List<CategoriaDTO> productos(Connection conexion) throws SQLException {
        Map<Integer, CategoriaDTO> categorias = new LinkedHashMap<>();
        String sql = """
                SELECT ca.id_categoria, ca.nombre AS categoria,
                       p.id_producto, p.nombre AS producto, p.precio, i.stock
                FROM producto p
                JOIN producto_categoria pc ON pc.id_producto = p.id_producto
                JOIN categoria ca ON ca.id_categoria = pc.id_categoria
                JOIN inventario i ON i.id_producto = p.id_producto
                ORDER BY ca.id_categoria, p.nombre
                """;

        try (Statement consulta = conexion.createStatement();
             ResultSet filas = consulta.executeQuery(sql)) {
            while (filas.next()) {
                int idCategoria = filas.getInt("id_categoria");
                CategoriaDTO categoria = categorias.get(idCategoria);

                if (categoria == null) {
                    categoria = new CategoriaDTO(
                            idCategoria, filas.getString("categoria"));
                    categorias.put(idCategoria, categoria);
                }

                ProductoDTO producto = new ProductoDTO(
                        filas.getInt("id_producto"),
                        filas.getString("producto"),
                        filas.getDouble("precio"),
                        filas.getInt("stock"));
                categoria.agregarProducto(producto);
            }
        }

        return new ArrayList<>(categorias.values());
    }

    // Recupera únicamente movimientos que existen y añade el nombre del producto
    public static List<MovimientoDTO> movimientos(Connection conexion) throws SQLException {
        List<MovimientoDTO> movimientos = new ArrayList<>();
        String sql = """
                SELECT m.id_movimiento, m.fecha, m.tipo, p.nombre AS producto
                FROM movimientos_stock m
                JOIN producto p ON p.id_producto = m.id_producto
                ORDER BY m.fecha, m.id_movimiento
                """;

        try (Statement consulta = conexion.createStatement();
             ResultSet filas = consulta.executeQuery(sql)) {
            while (filas.next()) {
                String fecha = filas.getTimestamp("fecha")
                        .toLocalDateTime()
                        .format(FORMATO_FECHA);
                MovimientoDTO movimiento = new MovimientoDTO(
                        filas.getInt("id_movimiento"),
                        filas.getString("producto"),
                        filas.getString("tipo"),
                        fecha);
                movimientos.add(movimiento);
            }
        }

        return movimientos;
    }

    // Recupera la cabecera y las líneas del pedido seleccionado
    public static PedidoDTO pedido(Connection conexion, int idPedido) throws SQLException {
        Map<ProductoDTO, Integer> productos = new LinkedHashMap<>();
        String nombreCliente = null;
        String correoCliente = null;
        String direccionCliente = null;
        String estadoPago = null;
        double precioTotal = 0.0;

        String sql = """
                SELECT pe.total,
                       cl.nombre AS cliente,
                       cl.email AS cliente_email,
                       (SELECT d1.calle || ', ' || d1.ciudad || ', ' || d1.codigo_postal
                        FROM cliente_direccion cd1
                        JOIN direccion d1 ON d1.id_direccion = cd1.id_direccion
                        WHERE cd1.id_cliente = cl.id_cliente
                        ORDER BY d1.id_direccion
                        FETCH FIRST 1 ROW ONLY) AS direccion1,
                       pa.metodo_pago,
                       pa.estado AS pago_estado,
                       pa.importe,
                       p.id_producto,
                       p.nombre AS producto,
                       d.cantidadProducto,
                       d.precio_unitario
                FROM pedido pe
                JOIN cliente cl ON cl.id_cliente = pe.id_cliente
                JOIN pago pa ON pa.id_pedido = pe.id_pedido
                JOIN detalle_pedido d ON d.id_pedido = pe.id_pedido
                JOIN producto p ON p.id_producto = d.id_producto
                WHERE pe.id_pedido = ?
                ORDER BY d.id_detalle
                """;

        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, idPedido);

            try (ResultSet filas = consulta.executeQuery()) {
                while (filas.next()) {
                    if (nombreCliente == null) {
                        nombreCliente = filas.getString("cliente");
                        correoCliente = filas.getString("cliente_email");
                        direccionCliente = filas.getString("direccion1");
                        precioTotal = filas.getDouble("total");
                        estadoPago = construirResumenPago(filas);
                    }

                    ProductoDTO producto = new ProductoDTO(
                            filas.getInt("id_producto"),
                            filas.getString("producto"),
                            filas.getDouble("precio_unitario"));
                    int cantidad = filas.getInt("cantidadProducto");
                    productos.put(producto, cantidad);
                }
            }
        }

        if (nombreCliente == null) {
            return null;
        }

        return new PedidoDTO(idPedido, nombreCliente, correoCliente,
                direccionCliente, productos, precioTotal, estadoPago);
    }

    private static String construirDireccion(ResultSet fila, String calle) throws SQLException {
        return calle + ", "
                + fila.getString("ciudad") + ", "
                + fila.getString("codigo_postal") + ", "
                + fila.getString("pais");
    }

    private static String construirResumenPago(ResultSet fila) throws SQLException {
        return fila.getString("metodo_pago")
                + " / " + fila.getString("pago_estado")
                + " / " + fila.getDouble("importe") + " €";
    }
}
