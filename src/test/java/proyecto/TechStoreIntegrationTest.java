package proyecto;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import proyecto.clasesTablas.DetallePedido;
import proyecto.clasesTablas.Producto;
import proyecto.dao.ClienteDAO;
import proyecto.dao.DetallePedidoDAO;
import proyecto.dao.InventarioDAO;
import proyecto.dao.ProductoDAO;
import proyecto.dto.CategoriaDTO;
import proyecto.dto.ClienteDTO;
import proyecto.dto.MovimientoDTO;
import proyecto.dto.PedidoDTO;
import proyecto.service.PedidoService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas aisladas: cada caso usa una base H2 en memoria y no toca la base de la aplicación. */
class TechStoreIntegrationTest {
    private Connection conexion;

    @BeforeEach
    void crearBaseTemporal() throws SQLException {
        String nombre = "techstore_test_" + UUID.randomUUID().toString().replace("-", "");
        conexion = DriverManager.getConnection("jdbc:h2:mem:" + nombre, "sa", "");
        crearEsquemaDePrueba();
    }

    @AfterEach
    void cerrarBaseTemporal() throws SQLException {
        if (conexion != null) conexion.close();
    }

    @Test
    void clienteDaoEncuentraUnClienteYDevuelveNullSiNoExiste() throws SQLException {
        insertarCliente(1, "Ana", "Pérez", "ana@example.test");

        var cliente = new ClienteDAO(conexion).buscarPorId(1);

        assertNotNull(cliente);
        assertEquals("Ana", cliente.getNombre());
        assertEquals("Pérez", cliente.getApellidos());
        assertEquals("ana@example.test", cliente.getEmail());
        assertNull(new ClienteDAO(conexion).buscarPorId(99));
    }

    @Test
    void productoDaoGuardaYRecuperaUnProducto() throws SQLException {
        Producto producto = new Producto(0, "Ratón gaming", 49.95, "Sensor óptico", true);

        int id = new ProductoDAO(conexion).insertar(producto);
        Producto recuperado = new ProductoDAO(conexion).buscarPorId(id);

        assertTrue(id > 0);
        assertNotNull(recuperado);
        assertEquals("Ratón gaming", recuperado.getNombre());
        assertEquals(49.95, recuperado.getPrecio(), 0.0001);
        assertTrue(recuperado.isActivo());
    }

    @Test
    void servicioCreaPedidoConVariasLineasActualizaStockYRegistraPagoPendiente() throws SQLException {
        insertarCliente(1, "Ana", "Pérez", "ana@example.test");
        insertarProducto(1, "Teclado", 20.00, 10);
        insertarProducto(2, "Ratón", 15.00, 8);

        var pedido = new PedidoService(conexion).realizarPedido(1,
                List.of(linea(1, 2), linea(2, 1)), "TARJETA");

        assertEquals("PENDIENTE", pedido.getEstado());
        assertEquals(55.00, pedido.getTotal(), 0.001);
        assertEquals(2, new DetallePedidoDAO(conexion).listarPorPedido(pedido.getIdPedido()).size());
        assertEquals(8, new InventarioDAO(conexion).buscarPorProducto(1).getStock());
        assertEquals(7, new InventarioDAO(conexion).buscarPorProducto(2).getStock());
        assertEquals(2, contar("movimientos_stock"));
        try (PreparedStatement p = conexion.prepareStatement(
                "SELECT estado,importe FROM pago WHERE id_pedido=?")) {
            p.setInt(1, pedido.getIdPedido());
            try (ResultSet r = p.executeQuery()) {
                assertTrue(r.next());
                assertEquals("PENDIENTE", r.getString("estado"));
                assertEquals(55.00, r.getDouble("importe"), 0.001);
            }
        }
    }

    @Test
    void errorAlGuardarPagoReviertePedidoLineasStockYMovimientos() throws SQLException {
        insertarCliente(1, "Ana", "Pérez", "ana@example.test");
        insertarProducto(1, "Teclado", 20.00, 10);
        insertarProducto(2, "Ratón", 15.00, 8);

        // Fuerza un error al final de la operación, después de modificar stock y movimientos.
        ejecutar("ALTER TABLE pago ADD CONSTRAINT importe_invalido CHECK (importe < 0)");

        Executable accionDePedido = new Executable() {
            @Override
            public void execute() throws Throwable {
                new PedidoService(conexion).realizarPedido(
                        1, List.of(linea(1, 1), linea(2, 1)), "EFECTIVO");
            }
        };
        assertThrows(SQLException.class, accionDePedido);

        assertEquals(0, contar("pedido"));
        assertEquals(0, contar("detalle_pedido"));
        assertEquals(0, contar("pago"));
        assertEquals(0, contar("movimientos_stock"));
        assertEquals(10, new InventarioDAO(conexion).buscarPorProducto(1).getStock());
        assertEquals(8, new InventarioDAO(conexion).buscarPorProducto(2).getStock());
    }

    @Test
    void consultasAgrupanClientesCategoriasYMovimientosEnDtos() throws SQLException {
        insertarCliente(1, "Ana", "Pérez", "ana@example.test");
        insertarDireccion(1, "Calle Uno", "Madrid");
        insertarDireccion(2, "Calle Dos", "Toledo");
        ejecutar("INSERT INTO cliente_direccion VALUES (1,1),(1,2)");
        ejecutar("INSERT INTO categoria VALUES (1,'Periféricos')");
        insertarProducto(1, "Ratón", 25.50, 7);
        ejecutar("INSERT INTO producto_categoria VALUES (1,1)");
        ejecutar("INSERT INTO movimientos_stock (id_producto,tipo,cantidadCambiada,fecha) "
                + "VALUES (1,'COMPRA',5,TIMESTAMP '2025-02-03 04:05:06')");

        Simulacion dao = new Simulacion(conexion);
        List<ClienteDTO> clientes = dao.clientesConDirecciones();
        List<CategoriaDTO> categorias = dao.productosPorCategoria();
        List<MovimientoDTO> movimientos = dao.historialMovimientos();

        assertEquals(1, clientes.size());
        assertEquals("Ana Pérez", clientes.get(0).nombre());
        assertEquals(2, clientes.get(0).direcciones().size());
        assertEquals("Periféricos", categorias.get(0).nombre());
        assertEquals("Ratón", categorias.get(0).productos().get(0).nombre());
        assertEquals(7, categorias.get(0).productos().get(0).stock());
        assertEquals(1, movimientos.size());
        assertEquals("Ratón", movimientos.get(0).nombreProducto());
        assertEquals("03:02:2025 04:05:06", movimientos.get(0).fecha());
    }

    @Test
    void consultaPedidoConstruyeDtoConClienteDireccionProductosYPago() throws SQLException {
        insertarCliente(1, "Ana", "Pérez", "ana@example.test");
        insertarDireccion(1, "Calle Uno", "Madrid");
        ejecutar("INSERT INTO cliente_direccion VALUES (1,1)");
        insertarProducto(1, "Teclado", 20.00, 5);
        insertarProducto(2, "Ratón", 15.00, 5);
        ejecutar("INSERT INTO pedido (id_pedido,id_cliente,estado,total) VALUES (1,1,'PENDIENTE',55.00)");
        ejecutar("INSERT INTO detalle_pedido (id_pedido,id_producto,cantidadProducto,precio_unitario) "
                + "VALUES (1,1,2,20.00),(1,2,1,15.00)");
        ejecutar("INSERT INTO pago (id_pedido,metodo_pago,estado,importe) "
                + "VALUES (1,'TARJETA','PENDIENTE',55.00)");

        PedidoDTO pedido = new Simulacion(conexion).pedidoCompleto(1);

        assertNotNull(pedido);
        assertEquals("Ana", pedido.nombreCliente());
        assertEquals("ana@example.test", pedido.emailCliente());
        assertEquals("Calle Uno, Madrid, 28001", pedido.direccion1());
        assertEquals(55.00, pedido.precioTotal(), 0.001);
        assertTrue(pedido.pago().contains("PENDIENTE"));
        assertEquals(2, pedido.productos().size());
        boolean incluyeDosTeclados = false;
        for (var linea : pedido.productos().entrySet()) {
            if (linea.getKey().nombre().equals("Teclado") && linea.getValue() == 2) {
                incluyeDosTeclados = true;
                break;
            }
        }
        assertTrue(incluyeDosTeclados);
    }

    private void crearEsquemaDePrueba() throws SQLException {
        ejecutar("""
                CREATE TABLE cliente (
                    id_cliente INT PRIMARY KEY,
                    nombre VARCHAR(80),
                    apellidos VARCHAR(120),
                    email VARCHAR(180),
                    telefono VARCHAR(30)
                )
                """);
        ejecutar("""
                CREATE TABLE direccion (
                    id_direccion INT PRIMARY KEY,
                    calle VARCHAR(180),
                    ciudad VARCHAR(100),
                    codigo_postal VARCHAR(15),
                    pais VARCHAR(80) DEFAULT 'España'
                )
                """);
        ejecutar("""
                CREATE TABLE cliente_direccion (
                    id_cliente INT,
                    id_direccion INT,
                    PRIMARY KEY (id_cliente, id_direccion)
                )
                """);
        ejecutar("CREATE TABLE categoria (id_categoria INT PRIMARY KEY, nombre VARCHAR(80))");
        ejecutar("""
                CREATE TABLE producto (
                    id_producto INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    nombre VARCHAR(160),
                    precio DECIMAL(10,2),
                    descripcion CLOB,
                    activo BOOLEAN
                )
                """);
        ejecutar("""
                CREATE TABLE producto_categoria (
                    id_producto INT,
                    id_categoria INT,
                    PRIMARY KEY (id_producto, id_categoria)
                )
                """);
        ejecutar("""
                CREATE TABLE inventario (
                    id_inventario INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    id_producto INT UNIQUE,
                    stock INT,
                    stock_minimo INT,
                    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
        ejecutar("""
                CREATE TABLE pedido (
                    id_pedido INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    id_cliente INT,
                    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    estado VARCHAR(20),
                    total DECIMAL(12,2)
                )
                """);
        ejecutar("""
                CREATE TABLE detalle_pedido (
                    id_detalle INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    id_pedido INT,
                    id_producto INT,
                    cantidadProducto INT,
                    precio_unitario DECIMAL(10,2),
                    UNIQUE (id_pedido, id_producto)
                )
                """);
        ejecutar("""
                CREATE TABLE movimientos_stock (
                    id_movimiento INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    id_producto INT,
                    tipo VARCHAR(20),
                    cantidadCambiada INT,
                    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
        ejecutar("""
                CREATE TABLE pago (
                    id_pago INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    id_pedido INT UNIQUE,
                    metodo_pago VARCHAR(30),
                    estado VARCHAR(20),
                    importe DECIMAL(12,2),
                    fecha_pago TIMESTAMP
                )
                """);
    }

    private void insertarCliente(int id, String nombre, String apellidos, String email) throws SQLException {
        try (PreparedStatement p = conexion.prepareStatement("INSERT INTO cliente VALUES (?,?,?,?,?)")) {
            p.setInt(1, id);
            p.setString(2, nombre);
            p.setString(3, apellidos);
            p.setString(4, email);
            p.setString(5, "600000000");
            p.executeUpdate();
        }
    }

    private void insertarDireccion(int id, String calle, String ciudad) throws SQLException {
        try (PreparedStatement p = conexion.prepareStatement("INSERT INTO direccion VALUES (?,?,?,?,?)")) {
            p.setInt(1, id);
            p.setString(2, calle);
            p.setString(3, ciudad);
            p.setString(4, "28001");
            p.setString(5, "España");
            p.executeUpdate();
        }
    }

    private void insertarProducto(int id, String nombre, double precio, int stock) throws SQLException {
        try (PreparedStatement p = conexion.prepareStatement(
                "INSERT INTO producto (id_producto,nombre,precio,descripcion,activo) VALUES (?,?,?,?,TRUE)")) {
            p.setInt(1, id);
            p.setString(2, nombre);
            p.setDouble(3, precio);
            p.setString(4, "Producto de prueba");
            p.executeUpdate();
        }
        try (PreparedStatement p = conexion.prepareStatement(
                "INSERT INTO inventario (id_producto,stock,stock_minimo) VALUES (?,?,2)")) {
            p.setInt(1, id);
            p.setInt(2, stock);
            p.executeUpdate();
        }
    }

    private DetallePedido linea(int idProducto, int cantidad) {
        return new DetallePedido(0, 0, idProducto, cantidad, 0.0);
    }

    private void ejecutar(String sql) throws SQLException {
        try (Statement sentencia = conexion.createStatement()) {
            sentencia.executeUpdate(sql);
        }
    }

    private int contar(String tabla) throws SQLException {
        try (Statement sentencia = conexion.createStatement();
             ResultSet resultados = sentencia.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            resultados.next();
            return resultados.getInt(1);
        }
    }
}
