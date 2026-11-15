package proyecto;

import proyecto.dao.BaseDAO;
import proyecto.database.ProcedimientosH2;
import proyecto.dto.CategoriaDTO;
import proyecto.dto.ClienteDTO;
import proyecto.dto.MovimientoDTO;
import proyecto.dto.PedidoDTO;
import proyecto.dto.ProductoDTO;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Ejecuta las consultas de la simulación

public class Simulacion extends BaseDAO {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd:MM:yyyy HH:mm:ss");

    public Simulacion(Connection conexion) {
        super(conexion);
    }

    // Lee el resultado del procedimiento y reúne las direcciones de cada cliente
    public List<ClienteDTO> clientesConDirecciones() throws SQLException {
        if (esH2()) {
            return ProcedimientosH2.clientes(conexion);
        }

        Map<Integer, ClienteDTO> clientes = new LinkedHashMap<>();
        String llamadaSQL = "{call TS_CLIENTES_DIRECCIONES()}";

        try (CallableStatement llamada = conexion.prepareCall(llamadaSQL);
             ResultSet filas = llamada.executeQuery()) {
            while (filas.next()) {
                int idCliente = filas.getInt("id_cliente");
                ClienteDTO cliente = clientes.get(idCliente);

                if (cliente == null) {
                    String nombreCompleto = filas.getString("nombre")
                            + " " + filas.getString("apellidos");
                    cliente = new ClienteDTO(
                            idCliente,
                            nombreCompleto,
                            filas.getString("email"));
                    clientes.put(idCliente, cliente);
                }

                // Si existe dirección, se añade al DTO del cliente correspondiente.
                String calle = filas.getString("calle");
                if (calle != null) {
                    String direccion = calle + ", "
                            + filas.getString("ciudad") + ", "
                            + filas.getString("codigo_postal") + ", "
                            + filas.getString("pais");
                    cliente.agregarDireccion(direccion);
                }
            }
        }

        return new ArrayList<>(clientes.values());
    }

    //Consulta los productos y los agrupa por categoría
    public List<CategoriaDTO> productosPorCategoria() throws SQLException {
        if (esH2()) {
            return ProcedimientosH2.productos(conexion);
        }

        Map<Integer, CategoriaDTO> categorias = new LinkedHashMap<>();
        String llamadaSQL = "{call TS_PRODUCTOS_STOCK()}";

        try (CallableStatement llamada = conexion.prepareCall(llamadaSQL);
             ResultSet filas = llamada.executeQuery()) {
            while (filas.next()) {
                agregarProductoDesdeFila(categorias, filas);
            }
        }

        return new ArrayList<>(categorias.values());
    }

    //Consulta únicamente los movimientos existentes y sus productos asociados
    public List<MovimientoDTO> historialMovimientos() throws SQLException {
        if (esH2()) {
            return ProcedimientosH2.movimientos(conexion);
        }

        List<MovimientoDTO> movimientos = new ArrayList<>();
        String llamadaSQL = "{call TS_MOVIMIENTOS_PRODUCTO()}";

        try (CallableStatement llamada = conexion.prepareCall(llamadaSQL);
             ResultSet filas = llamada.executeQuery()) {
            while (filas.next()) {
                MovimientoDTO movimiento = convertirMovimiento(filas);
                movimientos.add(movimiento);
            }
        }

        return movimientos;
    }

    // Consulta un pedido
    public PedidoDTO pedidoCompleto(int idPedido) throws SQLException {
        if (esH2()) {
            return ProcedimientosH2.pedido(conexion, idPedido);
        }

        Map<ProductoDTO, Integer> productos = new LinkedHashMap<>();
        String nombreCliente = null;
        String correoCliente = null;
        String direccion = null;
        String resumenPago = null;
        double precioTotal = 0.0;

        String llamadaSQL = "{call TS_PEDIDO_COMPLETO(?)}";
        try (CallableStatement llamada = conexion.prepareCall(llamadaSQL)) {
            llamada.setInt(1, idPedido);

            try (ResultSet filas = llamada.executeQuery()) {
                while (filas.next()) {
                    if (nombreCliente == null) {
                        nombreCliente = filas.getString("cliente");
                        correoCliente = filas.getString("cliente_email");
                        direccion = filas.getString("direccion1");
                        precioTotal = filas.getDouble("total");
                        resumenPago = construirResumenPago(filas);
                    }

                    ProductoDTO producto = convertirProductoDePedido(filas);
                    int cantidad = filas.getInt("cantidadProducto");
                    productos.put(producto, cantidad);
                }
            }
        }

        if (nombreCliente == null) {
            return null;
        }

        return new PedidoDTO(idPedido, nombreCliente, correoCliente,
                direccion, productos, precioTotal, resumenPago);
    }

    private void agregarProductoDesdeFila(Map<Integer, CategoriaDTO> categorias, ResultSet fila)
            throws SQLException {
        int idCategoria = fila.getInt("id_categoria");
        CategoriaDTO categoria = categorias.get(idCategoria);

        if (categoria == null) {
            categoria = new CategoriaDTO(idCategoria, fila.getString("categoria"));
            categorias.put(idCategoria, categoria);
        }

        ProductoDTO producto = new ProductoDTO(
                fila.getInt("id_producto"),
                fila.getString("producto"),
                fila.getDouble("precio"),
                fila.getInt("stock"));
        categoria.agregarProducto(producto);
    }

    private MovimientoDTO convertirMovimiento(ResultSet fila) throws SQLException {
        String fecha = fila.getTimestamp("fecha")
                .toLocalDateTime()
                .format(FORMATO_FECHA);

        return new MovimientoDTO(
                fila.getInt("id_movimiento"),
                fila.getString("producto"),
                fila.getString("tipo"),
                fecha);
    }

    private ProductoDTO convertirProductoDePedido(ResultSet fila) throws SQLException {
        return new ProductoDTO(
                fila.getInt("id_producto"),
                fila.getString("producto"),
                fila.getDouble("precio_unitario"));
    }

    private String construirResumenPago(ResultSet fila) throws SQLException {
        return fila.getString("metodo_pago")
                + " / " + fila.getString("pago_estado")
                + " / " + fila.getDouble("importe") + " €";
    }

    private boolean esH2() throws SQLException {
        String nombreMotor = conexion.getMetaData().getDatabaseProductName();
        return "H2".equalsIgnoreCase(nombreMotor);
    }
}
