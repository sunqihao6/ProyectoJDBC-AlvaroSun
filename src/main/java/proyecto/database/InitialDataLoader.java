package proyecto.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/** Restaura los registros iniciales que corresponden al esquema actual de Tech Store. */
public final class InitialDataLoader {
    private static final Logger logger = LogManager.getLogger(InitialDataLoader.class);

    private InitialDataLoader() {
    }

    /** Borra los datos de la simulación y vuelve a cargar el conjunto inicial. */
    public static void restaurarYCargar(Connection connection, boolean mysql) throws SQLException {
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            // PASO 1: borrar primero las filas dependientes de otras tablas.
            limpiarTablas(connection);

            // PASO 2: cargar datos principales antes de sus relaciones.
            insertarClientes(connection);
            insertarDirecciones(connection);
            insertarRelacionesClienteDireccion(connection);
            insertarProductos(connection);
            insertarCategorias(connection);
            insertarRelacionesProductoCategoria(connection);
            insertarInventario(connection);
            insertarPedidos(connection);
            insertarDetallesPedido(connection);
            insertarMovimientosStock(connection);
            insertarPagos(connection);

            // PASO 3: aplicar el conjunto inicial como una sola transacción.
            connection.commit();
        } catch (SQLException | RuntimeException error) {
            connection.rollback();
            throw error;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }

        // PASO 4: preparar las columnas identity/autoincrement para nuevas filas.
        reiniciarIdentificadores(connection, mysql);
        logger.info("Datos restaurados al estado inicial de la simulación");
    }

    private static void limpiarTablas(Connection connection) throws SQLException {
        // El orden respeta las claves foráneas declaradas en DatabaseSchemaInitializer.
        String[] tables = {
                "pago", "detalle_pedido", "pedido", "movimientos_stock", "inventario",
                "producto_categoria", "cliente_direccion", "categoria", "producto",
                "direccion", "cliente"
        };
        try (Statement statement = connection.createStatement()) {
            for (String table : tables) {
                statement.executeUpdate("DELETE FROM " + table);
            }
        }
    }

    private static void insertarClientes(Connection connection) throws SQLException {
        String sql = "INSERT INTO cliente (id_cliente,nombre,apellidos,email,telefono) "
                + "VALUES (?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            agregarCliente(statement, 1, "Alba", "Sánchez Ruiz", "alba.sanchez@example.test", "600111222");
            agregarCliente(statement, 2, "Marcos", "López Díaz", "marcos.lopez@example.test", "600333444");
            statement.executeBatch();
        }
    }

    private static void agregarCliente(PreparedStatement statement, long id, String name,
                                  String surname, String email, String phone) throws SQLException {
        statement.setLong(1, id);
        statement.setString(2, name);
        statement.setString(3, surname);
        statement.setString(4, email);
        statement.setString(5, phone);
        statement.addBatch();
    }

    private static void insertarDirecciones(Connection connection) throws SQLException {
        String sql = "INSERT INTO direccion "
                + "(id_direccion,calle,ciudad,codigo_postal,pais) VALUES (?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            agregarDireccion(statement, 1, "Calle Mayor 10", "Madrid", "28013");
            agregarDireccion(statement, 2, "Avenida del Puerto 25", "Valencia", "46021");
            statement.executeBatch();
        }
    }

    private static void agregarDireccion(PreparedStatement statement, long id, String street,
                                   String city, String postalCode) throws SQLException {
        statement.setLong(1, id);
        statement.setString(2, street);
        statement.setString(3, city);
        statement.setString(4, postalCode);
        statement.setString(5, "España");
        statement.addBatch();
    }

    private static void insertarRelacionesClienteDireccion(Connection connection) throws SQLException {
        String sql = "INSERT INTO cliente_direccion (id_cliente,id_direccion) VALUES (?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (long id = 1; id <= 2; id++) {
                statement.setLong(1, id);
                statement.setLong(2, id);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void insertarProductos(Connection connection) throws SQLException {
        String sql = "INSERT INTO producto (id_producto,nombre,precio,descripcion,activo) "
                + "VALUES (?,?,?,?,?)";
        String[][] products = {
                {"GeForce RTX 4070 Super", "699.99", "Tarjeta gráfica para gaming QHD"},
                {"Ryzen 7 7800X3D", "399.99", "Procesador AM5 para gaming"},
                {"B650 Gaming Plus WiFi", "189.99", "Placa base AM5 con soporte DDR5"},
                {"Vengeance DDR5 32 GB", "119.99", "Kit de memoria 2x16 GB"},
                {"Odyssey G5 27 pulgadas", "279.99", "Monitor QHD de alta frecuencia"},
                {"DeathAdder V3", "79.99", "Ratón gaming ergonómico"}
        };
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < products.length; i++) {
                statement.setLong(1, i + 1);
                statement.setString(2, products[i][0]);
                statement.setBigDecimal(3, new BigDecimal(products[i][1]));
                statement.setString(4, products[i][2]);
                statement.setBoolean(5, true);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void insertarCategorias(Connection connection) throws SQLException {
        String sql = "INSERT INTO categoria (id_categoria,nombre,descripcion) VALUES (?,?,?)";
        String[][] categories = {
                {"Tarjetas gráficas", "GPU para equipos de sobremesa"},
                {"Procesadores", "CPU para equipos compatibles"},
                {"Placas base", "Placas base y chipsets"},
                {"Memoria RAM", "Módulos de memoria"},
                {"Monitores", "Pantallas para ordenador"},
                {"Periféricos", "Ratones, teclados y accesorios"}
        };
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < categories.length; i++) {
                statement.setLong(1, i + 1);
                statement.setString(2, categories[i][0]);
                statement.setString(3, categories[i][1]);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void insertarRelacionesProductoCategoria(Connection connection) throws SQLException {
        String sql = "INSERT INTO producto_categoria (id_producto,id_categoria) VALUES (?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (long id = 1; id <= 6; id++) {
                statement.setLong(1, id);
                statement.setLong(2, id);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void insertarInventario(Connection connection) throws SQLException {
        String sql = "INSERT INTO inventario (id_inventario,id_producto,stock,stock_minimo) "
                + "VALUES (?,?,?,?)";
        int[] stocks = {7, 12, 8, 20, 6, 18};
        int[] minimumStocks = {2, 3, 2, 5, 2, 4};
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < stocks.length; i++) {
                statement.setLong(1, i + 1);
                statement.setLong(2, i + 1);
                statement.setInt(3, stocks[i]);
                statement.setInt(4, minimumStocks[i]);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void insertarDetallesPedido(Connection connection) throws SQLException {
        String sql = "INSERT INTO detalle_pedido "
                + "(id_detalle,id_pedido,id_producto,cantidadProducto,precio_unitario) "
                + "VALUES (?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, 1);
            statement.setLong(2, 1);
            statement.setLong(3, 1);
            statement.setInt(4, 1);
            statement.setBigDecimal(5, new BigDecimal("699.99"));
            statement.addBatch();

            statement.setLong(1, 2);
            statement.setLong(2, 1);
            statement.setLong(3, 4);
            statement.setInt(4, 2);
            statement.setBigDecimal(5, new BigDecimal("119.99"));
            statement.addBatch();

            statement.executeBatch();
        }
    }

    private static void insertarPedidos(Connection connection) throws SQLException {
        String sql = "INSERT INTO pedido (id_pedido,id_cliente,fecha,estado,total) "
                + "VALUES (?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, 1);
            statement.setLong(2, 1);
            statement.setTimestamp(3, convertirFechaHora("2026-11-01T12:00:00"));
            statement.setString(4, "COMPLETADO");
            statement.setBigDecimal(5, new BigDecimal("939.97"));
            statement.executeUpdate();
        }
    }

    private static void insertarMovimientosStock(Connection connection) throws SQLException {
        String sql = "INSERT INTO movimientos_stock "
                + "(id_movimiento,id_producto,tipo,cantidadCambiada,fecha) VALUES (?,?,?,?,?)";
        String[] types = {"COMPRA", "VENTA", "COMPRA", "COMPRA", "COMPRA", "VENTA", "COMPRA", "COMPRA"};
        int[] products = {1, 1, 2, 3, 4, 4, 5, 6};
        int[] quantities = {8, 1, 12, 8, 22, 2, 6, 18};
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < types.length; i++) {
                statement.setLong(1, i + 1);
                statement.setInt(2, products[i]);
                statement.setString(3, types[i]);
                statement.setInt(4, quantities[i]);
                statement.setTimestamp(5, convertirFechaHora(i == 1 || i == 5
                        ? "2026-11-01T12:00:00" : "2026-10-20T09:00:00"));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void insertarPagos(Connection connection) throws SQLException {
        String sql = "INSERT INTO pago "
                + "(id_pago,id_pedido,metodo_pago,estado,importe,fecha_pago) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, 1);
            statement.setLong(2, 1);
            statement.setString(3, "TARJETA");
            statement.setString(4, "COMPLETADO");
            statement.setBigDecimal(5, new BigDecimal("939.97"));
            statement.setTimestamp(6, convertirFechaHora("2026-11-01T12:00:00"));
            statement.executeUpdate();
        }
    }

    private static void reiniciarIdentificadores(Connection connection, boolean mysql) throws SQLException {
        String[][] identities = {
                {"cliente", "id_cliente", "3"}, {"direccion", "id_direccion", "3"},
                {"producto", "id_producto", "7"}, {"categoria", "id_categoria", "7"},
                {"inventario", "id_inventario", "7"},
                {"detalle_pedido", "id_detalle", "3"},
                {"pedido", "id_pedido", "2"}, {"movimientos_stock", "id_movimiento", "9"},
                {"pago", "id_pago", "2"}
        };
        try (Statement statement = connection.createStatement()) {
            for (String[] identity : identities) {
                String sql = mysql
                        ? "ALTER TABLE " + identity[0] + " AUTO_INCREMENT = " + identity[2]
                        : "ALTER TABLE " + identity[0] + " ALTER COLUMN " + identity[1]
                                + " RESTART WITH " + identity[2];
                statement.execute(sql);
            }
        }
    }

    private static Timestamp convertirFechaHora(String value) {
        return Timestamp.valueOf(LocalDateTime.parse(value));
    }
}
