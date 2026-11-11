package proyecto.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/** Restaura los datos iniciales de la simulación sin recrear el esquema. */
public final class InitialDataLoader {
    private static final Logger logger = LogManager.getLogger(InitialDataLoader.class);

    private InitialDataLoader() {
    }

    public static void resetAndLoad(Connection connection, boolean mysql) throws SQLException {
        // PASO 1: iniciar la transacción de restauración.
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            // PASO 2: limpiar registros de la simulación, respetando las claves foráneas.
            deleteSimulationData(connection);

            // PASO 3: insertar los registros base en el orden de las relaciones.
            insertClients(connection);
            insertProfiles(connection);
            insertAddresses(connection);
            insertProducts(connection);
            insertCategories(connection);
            insertProductCategories(connection);
            insertInventory(connection);
            insertOrders(connection);
            insertOrderDetails(connection);
            insertStockMovements(connection);
            insertPayments(connection);

            // PASO 4: confirmar la restauración completa al estado inicial.
            connection.commit();
        } catch (SQLException | RuntimeException error) {
            // PASO 5: si falla una operación, recuperar el estado anterior.
            connection.rollback();
            throw error;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }

        // PASO 6: alinear los contadores con los IDs iniciales de ejemplo.
        if (mysql) {
            restartMySqlIdentities(connection);
        } else {
            restartH2Identities(connection);
        }

        logger.info("Datos restaurados al estado inicial de la simulación");
    }

    private static void deleteSimulationData(Connection connection) throws SQLException {
        // Primero se borran las tablas hijas; al final, las tablas principales.
        String[] tablesInDeleteOrder = {
                "pagos",
                "detalle_pedido",
                "movimientos_stock",
                "inventario",
                "pedidos",
                "producto_categoria",
                "direcciones",
                "perfil_cliente",
                "categorias",
                "productos",
                "clientes"
        };

        try (Statement statement = connection.createStatement()) {
            for (String table : tablesInDeleteOrder) {
                statement.executeUpdate("DELETE FROM " + table);
            }
        }
    }

    private static void restartMySqlIdentities(Connection connection) throws SQLException {
        // Se ejecuta después de la carga; cada contador queda detrás de los IDs semilla.
        String[] statements = {
                "ALTER TABLE clientes AUTO_INCREMENT = 3",
                "ALTER TABLE direcciones AUTO_INCREMENT = 3",
                "ALTER TABLE productos AUTO_INCREMENT = 7",
                "ALTER TABLE categorias AUTO_INCREMENT = 7",
                "ALTER TABLE inventario AUTO_INCREMENT = 7",
                "ALTER TABLE pedidos AUTO_INCREMENT = 2",
                "ALTER TABLE detalle_pedido AUTO_INCREMENT = 3",
                "ALTER TABLE movimientos_stock AUTO_INCREMENT = 9",
                "ALTER TABLE pagos AUTO_INCREMENT = 2"
        };

        try (Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.execute(sql);
            }
        }
    }

    private static void insertClients(Connection connection) throws SQLException {
        String sql = "INSERT INTO clientes "
                + "(id_cliente,nombre,apellidos,email,telefono) VALUES (?,?,?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            // Cliente 1
            statement.setLong(1, 1);
            statement.setString(2, "Alba");
            statement.setString(3, "Sánchez Ruiz");
            statement.setString(4, "alba.sanchez@example.test");
            statement.setString(5, "600111222");
            statement.addBatch();

            // Cliente 2
            statement.setLong(1, 2);
            statement.setString(2, "Marcos");
            statement.setString(3, "López Díaz");
            statement.setString(4, "marcos.lopez@example.test");
            statement.setString(5, "600333444");
            statement.addBatch();

            statement.executeBatch();
        }
    }

    private static void insertProfiles(Connection connection) throws SQLException {
        String sql = "INSERT INTO perfil_cliente "
                + "(id_cliente,fecha_nacimiento,tipo_cliente) VALUES (?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, 1);
            statement.setDate(2, Date.valueOf("2001-04-12"));
            statement.setString(3, "PARTICULAR");
            statement.addBatch();

            statement.setLong(1, 2);
            statement.setDate(2, Date.valueOf("1998-09-25"));
            statement.setString(3, "PROFESIONAL");
            statement.addBatch();

            statement.executeBatch();
        }
    }

    private static void insertAddresses(Connection connection) throws SQLException {
        String sql = "INSERT INTO direcciones "
                + "(id_direccion,id_cliente,calle,ciudad,codigo_postal,pais) "
                + "VALUES (?,?,?,?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, 1);
            statement.setLong(2, 1);
            statement.setString(3, "Calle Mayor 10");
            statement.setString(4, "Madrid");
            statement.setString(5, "28013");
            statement.setString(6, "España");
            statement.addBatch();

            statement.setLong(1, 2);
            statement.setLong(2, 2);
            statement.setString(3, "Avenida del Puerto 25");
            statement.setString(4, "Valencia");
            statement.setString(5, "46021");
            statement.setString(6, "España");
            statement.addBatch();

            statement.executeBatch();
        }
    }

    private static void insertProducts(Connection connection) throws SQLException {
        String sql = "INSERT INTO productos "
                + "(id_producto,nombre,marca,precio,descripcion,tipo_producto,"
                + "socket,tipo_ram,capacidad,interfaz) VALUES (?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            addProduct(statement, 1, "GeForce RTX 4070 Super", "NVIDIA", "699.99",
                    "Tarjeta gráfica para gaming QHD", "GPU", null, null, "12 GB", "PCIe 4.0");
            addProduct(statement, 2, "Ryzen 7 7800X3D", "AMD", "399.99",
                    "Procesador AM5 para gaming", "CPU", "AM5", "DDR5", null, "PCIe 5.0");
            addProduct(statement, 3, "B650 Gaming Plus WiFi", "MSI", "189.99",
                    "Placa base AM5 con soporte DDR5", "PLACA_BASE", "AM5", "DDR5", null, "AM5");
            addProduct(statement, 4, "Vengeance DDR5 32 GB", "Corsair", "119.99",
                    "Kit de memoria 2x16 GB", "RAM", null, "DDR5", "32 GB", "DIMM");
            addProduct(statement, 5, "Odyssey G5 27 pulgadas", "Samsung", "279.99",
                    "Monitor QHD de alta frecuencia", "MONITOR", null, null,
                    "27 pulgadas", "DisplayPort");
            addProduct(statement, 6, "DeathAdder V3", "Razer", "79.99",
                    "Ratón gaming ergonómico", "RATON", null, null, null, "USB");

            statement.executeBatch();
        }
    }

    private static void addProduct(PreparedStatement statement, long id, String name,
                                   String brand, String price, String description,
                                   String type, String socket, String ram,
                                   String capacity, String interfaceType) throws SQLException {
        statement.setLong(1, id);
        statement.setString(2, name);
        statement.setString(3, brand);
        statement.setBigDecimal(4, new BigDecimal(price));
        statement.setString(5, description);
        statement.setString(6, type);
        statement.setString(7, socket);
        statement.setString(8, ram);
        statement.setString(9, capacity);
        statement.setString(10, interfaceType);
        statement.addBatch();
    }

    private static void insertCategories(Connection connection) throws SQLException {
        String sql = "INSERT INTO categorias (id_categoria,nombre,descripcion) VALUES (?,?,?)";
        String[][] categories = {
                {"Tarjetas gráficas", "GPU para equipos de sobremesa"},
                {"Procesadores", "CPU para equipos compatibles"},
                {"Placas base", "Placas base y chipsets"},
                {"Memoria RAM", "Módulos de memoria"},
                {"Monitores", "Pantallas para ordenador"},
                {"Periféricos", "Ratones, teclados y accesorios"}
        };

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < categories.length; index++) {
                statement.setLong(1, index + 1);
                statement.setString(2, categories[index][0]);
                statement.setString(3, categories[index][1]);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void insertProductCategories(Connection connection) throws SQLException {
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

    private static void insertInventory(Connection connection) throws SQLException {
        String sql = "INSERT INTO inventario "
                + "(id_inventario,id_producto,stock,stock_minimo) VALUES (?,?,?,?)";
        int[] stocks = {7, 12, 8, 20, 6, 18};
        int[] minimumStocks = {2, 3, 2, 5, 2, 4};

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < stocks.length; index++) {
                statement.setLong(1, index + 1);
                statement.setLong(2, index + 1);
                statement.setInt(3, stocks[index]);
                statement.setInt(4, minimumStocks[index]);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void insertOrders(Connection connection) throws SQLException {
        String sql = "INSERT INTO pedidos (id_pedido,id_cliente,fecha,estado,total) "
                + "VALUES (?,?,?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, 1);
            statement.setLong(2, 1);
            statement.setTimestamp(3, timestamp("2026-11-01T12:00:00"));
            statement.setString(4, "COMPLETADO");
            statement.setBigDecimal(5, new BigDecimal("939.97"));
            statement.executeUpdate();
        }
    }

    private static void insertOrderDetails(Connection connection) throws SQLException {
        String sql = "INSERT INTO detalle_pedido "
                + "(id_detalle,id_pedido,id_producto,cantidad,precio_unitario) "
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

    private static void insertStockMovements(Connection connection) throws SQLException {
        String sql = "INSERT INTO movimientos_stock "
                + "(id_movimiento,id_producto,tipo,cantidad,fecha,motivo,id_pedido) "
                + "VALUES (?,?,?,?,?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            addStockMovement(statement, 1, 1, "ENTRADA", 8,
                    "2026-10-20T09:00:00", "Carga inicial", null);
            addStockMovement(statement, 2, 1, "SALIDA", 1,
                    "2026-11-01T12:00:00", "Venta en pedido 1", 1L);
            addStockMovement(statement, 3, 2, "ENTRADA", 12,
                    "2026-10-20T09:00:00", "Carga inicial", null);
            addStockMovement(statement, 4, 3, "ENTRADA", 8,
                    "2026-10-20T09:00:00", "Carga inicial", null);
            addStockMovement(statement, 5, 4, "ENTRADA", 22,
                    "2026-10-20T09:00:00", "Carga inicial", null);
            addStockMovement(statement, 6, 4, "SALIDA", 2,
                    "2026-11-01T12:00:00", "Venta en pedido 1", 1L);
            addStockMovement(statement, 7, 5, "ENTRADA", 6,
                    "2026-10-20T09:00:00", "Carga inicial", null);
            addStockMovement(statement, 8, 6, "ENTRADA", 18,
                    "2026-10-20T09:00:00", "Carga inicial", null);
            statement.executeBatch();
        }
    }

    private static void addStockMovement(PreparedStatement statement, long id, long productId,
                                         String type, int quantity, String dateTime,
                                         String reason, Long orderId) throws SQLException {
        statement.setLong(1, id);
        statement.setLong(2, productId);
        statement.setString(3, type);
        statement.setInt(4, quantity);
        statement.setTimestamp(5, timestamp(dateTime));
        statement.setString(6, reason);
        if (orderId == null) {
            statement.setNull(7, java.sql.Types.BIGINT);
        } else {
            statement.setLong(7, orderId);
        }
        statement.addBatch();
    }

    private static void insertPayments(Connection connection) throws SQLException {
        String sql = "INSERT INTO pagos "
                + "(id_pago,id_pedido,metodo_pago,estado,importe,fecha_pago) "
                + "VALUES (?,?,?,?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, 1);
            statement.setLong(2, 1);
            statement.setString(3, "TARJETA");
            statement.setString(4, "COMPLETADO");
            statement.setBigDecimal(5, new BigDecimal("939.97"));
            statement.setTimestamp(6, timestamp("2026-11-01T12:00:00"));
            statement.executeUpdate();
        }
    }

    private static void restartH2Identities(Connection connection) throws SQLException {
        String[] statements = {
                "ALTER TABLE clientes ALTER COLUMN id_cliente RESTART WITH 3",
                "ALTER TABLE direcciones ALTER COLUMN id_direccion RESTART WITH 3",
                "ALTER TABLE productos ALTER COLUMN id_producto RESTART WITH 7",
                "ALTER TABLE categorias ALTER COLUMN id_categoria RESTART WITH 7",
                "ALTER TABLE inventario ALTER COLUMN id_inventario RESTART WITH 7",
                "ALTER TABLE pedidos ALTER COLUMN id_pedido RESTART WITH 2",
                "ALTER TABLE detalle_pedido ALTER COLUMN id_detalle RESTART WITH 3",
                "ALTER TABLE movimientos_stock ALTER COLUMN id_movimiento RESTART WITH 9",
                "ALTER TABLE pagos ALTER COLUMN id_pago RESTART WITH 2"
        };

        try (Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.execute(sql);
            }
        }
    }

    private static Timestamp timestamp(String value) {
        return Timestamp.valueOf(LocalDateTime.parse(value));
    }
}
