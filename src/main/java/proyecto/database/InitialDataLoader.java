package proyecto.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public final class InitialDataLoader {
    private static final Logger LOGGER = LogManager.getLogger(InitialDataLoader.class);

    private InitialDataLoader() {
    }

    // Borra los datos anteriores y vuelve a insertar el estado inicial
    public static void restaurarYCargar(Connection conexion, boolean mysql) throws SQLException {
        boolean autoCommitAnterior = conexion.getAutoCommit();
        conexion.setAutoCommit(false);

        try {
            limpiarTablas(conexion);
            insertarClientes(conexion);
            insertarDirecciones(conexion);
            relacionarClientesYDirecciones(conexion);
            insertarCategorias(conexion);
            insertarProductos(conexion);
            relacionarProductosYCategorias(conexion);
            insertarInventario(conexion);
            insertarMovimientosStock(conexion);


            conexion.commit();
        } catch (SQLException | RuntimeException error) {
            conexion.rollback();
            throw error;
        } finally {
            conexion.setAutoCommit(autoCommitAnterior);
        }

        reiniciarIdentificadores(conexion, mysql);
        LOGGER.info("Datos iniciales cargados: 5 categorías, 13 productos, 5 clientes y 2 pedidos");
    }

    private static void limpiarTablas(Connection conexion) throws SQLException {
        String[] tablasEnOrden = {
                "pago",
                "detalle_pedido",
                "pedido",
                "movimientos_stock",
                "inventario",
                "producto_categoria",
                "cliente_direccion",
                "categoria",
                "producto",
                "direccion",
                "cliente"
        };

        try (Statement sentencia = conexion.createStatement()) {
            for (String tabla : tablasEnOrden) {
                sentencia.executeUpdate("DELETE FROM " + tabla);
            }
        }
    }

    private static void insertarClientes(Connection conexion) throws SQLException {
        String sql = "INSERT INTO cliente VALUES (?,?,?,?,?)";
        Object[][] filas = {
                {1, "Alba", "Sánchez Ruiz", "alba@example.test", "600111222"},
                {2, "Marcos", "López Díaz", "marcos@example.test", "600333444"},
                {3, "Lucía", "Martín Ortega", "lucia@example.test", "611222333"},
                {4, "Diego", "Navarro Gil", "diego@example.test", "622333444"},
                {5, "Paula", "Serrano Vidal", "paula@example.test", "633444555"}
        };
        ejecutarBatch(conexion, sql, filas);
    }

    private static void insertarDirecciones(Connection conexion) throws SQLException {
        String sql = "INSERT INTO direccion VALUES (?,?,?,?,?)";
        Object[][] filas = {
                {1, "Calle Mayor 10", "Madrid", "28013", "España"},
                {2, "Av. del Puerto 25", "Valencia", "46021", "España"},
                {3, "Calle Estación 18", "Sevilla", "41001", "España"},
                {4, "Gran Vía 42", "Bilbao", "48011", "España"},
                {5, "Calle Colón 7", "Zaragoza", "50001", "España"},
                {6, "Rúa do Príncipe 15", "Vigo", "36202", "España"},
                {7, "Paseo de Gracia 101", "Barcelona", "08008", "España"},
                {8, "Calle Larios 9", "Málaga", "29005", "España"}
        };
        ejecutarBatch(conexion, sql, filas);
    }

    private static void relacionarClientesYDirecciones(Connection conexion) throws SQLException {
        String sql = "INSERT INTO cliente_direccion (id_cliente,id_direccion) VALUES (?,?)";
        Object[][] relaciones = {
                {1, 1}, {1, 2},
                {2, 3}, {2, 4},
                {3, 5},
                {4, 6},
                {5, 7}, {5, 8}
        };
        ejecutarBatch(conexion, sql, relaciones);
    }

    private static void insertarCategorias(Connection conexion) throws SQLException {
        String sql = "INSERT INTO categoria VALUES (?,?,?)";
        Object[][] filas = {
                {1, "Componentes", "Procesadores, gráficas y memoria"},
                {2, "Periféricos", "Ratones, teclados y auriculares"},
                {3, "Monitores", "Pantallas para ordenador"},
                {4, "Almacenamiento", "Unidades SSD y discos"},
                {5, "Refrigeración y energía", "Fuentes y refrigeración"}
        };
        ejecutarBatch(conexion, sql, filas);
    }

    private static void insertarProductos(Connection conexion) throws SQLException {
        String sql = "INSERT INTO producto (id_producto,nombre,precio,descripcion,activo) "
                + "VALUES (?,?,?,?,?)";
        Object[][] filas = {
                {1, "GeForce RTX 4070 Super", 699.99, "Tarjeta gráfica QHD", true},
                {2, "Radeon RX 7800 XT", 549.99, "Tarjeta gráfica gaming", true},
                {3, "Ryzen 7 7800X3D", 399.99, "Procesador AM5", true},
                {4, "Core i5-14600K", 329.99, "Procesador Intel", true},
                {5, "Vengeance DDR5 32 GB", 119.99, "Kit de memoria 2x16 GB", true},
                {6, "DeathAdder V3", 79.99, "Ratón gaming ergonómico", true},
                {7, "G Pro X TKL", 149.99, "Teclado mecánico compacto", true},
                {8, "Cloud III", 99.99, "Auriculares con micrófono", true},
                {9, "Odyssey G5 27 pulgadas", 279.99, "Monitor QHD", true},
                {10, "UltraGear 24 pulgadas", 189.99, "Monitor Full HD", true},
                {11, "990 EVO 1 TB", 99.99, "SSD NVMe", true},
                {12, "IronWolf 4 TB", 109.99, "Disco duro interno", true},
                {13, "RM750e 750 W", 119.99, "Fuente modular 80 Plus Gold", true}
        };
        ejecutarBatch(conexion, sql, filas);
    }

    private static void relacionarProductosYCategorias(Connection conexion) throws SQLException {
        String sql = "INSERT INTO producto_categoria (id_producto,id_categoria) VALUES (?,?)";
        int[] idsCategoria = {1, 1, 1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 5};
        Object[][] relaciones = new Object[idsCategoria.length][2];

        for (int indice = 0; indice < idsCategoria.length; indice++) {
            relaciones[indice][0] = indice + 1;
            relaciones[indice][1] = idsCategoria[indice];
        }

        ejecutarBatch(conexion, sql, relaciones);
    }

    private static void insertarInventario(Connection conexion) throws SQLException {
        String sql = "INSERT INTO inventario (id_inventario,id_producto,stock,stock_minimo) "
                + "VALUES (?,?,?,?)";
        int[] existencias = {24, 22, 23, 24, 23, 21, 26, 28, 29, 30, 29, 32, 33};
        Object[][] filas = new Object[existencias.length][4];

        for (int indice = 0; indice < existencias.length; indice++) {
            int identificador = indice + 1;
            filas[indice][0] = identificador;
            filas[indice][1] = identificador;
            filas[indice][2] = existencias[indice];
            filas[indice][3] = 3;
        }

        ejecutarBatch(conexion, sql, filas);
    }



    private static void insertarMovimientosStock(Connection conexion) throws SQLException {
        String sql = "INSERT INTO movimientos_stock "
                + "(id_movimiento,id_producto,tipo,cantidadCambiada,fecha) "
                + "VALUES (?,?,?,?,CURRENT_TIMESTAMP)";
        Object[][] filas = {
                {1, 1, "COMPRA", 25}, {2, 1, "VENTA", 1},
                {3, 5, "COMPRA", 25}, {4, 5, "VENTA", 2},
                {5, 6, "AJUSTE", 22}, {6, 6, "VENTA", 1},
                {7, 7, "COMPRA", 27}, {8, 7, "VENTA", 1},
                {9, 11, "COMPRA", 31}, {10, 11, "VENTA", 2}
        };
        ejecutarBatch(conexion, sql, filas);
    }


    //Ejecuta una inserción por lotes con los parámetros de cada fila
    private static void ejecutarBatch(Connection conexion, String sql, Object[][] filas)
            throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (Object[] fila : filas) {
                for (int indiceParametro = 0; indiceParametro < fila.length; indiceParametro++) {
                    sentencia.setObject(indiceParametro + 1, fila[indiceParametro]);
                }
                sentencia.addBatch();
            }
            sentencia.executeBatch();
        }
    }

    private static void reiniciarIdentificadores(Connection conexion, boolean mysql)
            throws SQLException {
        String[][] columnas = {
                {"cliente", "id_cliente", "6"},
                {"direccion", "id_direccion", "9"},
                {"categoria", "id_categoria", "6"},
                {"producto", "id_producto", "14"},
                {"inventario", "id_inventario", "14"},
                {"pedido", "id_pedido", "3"},
                {"detalle_pedido", "id_detalle", "6"},
                {"movimientos_stock", "id_movimiento", "11"},
                {"pago", "id_pago", "3"}
        };

        try (Statement sentencia = conexion.createStatement()) {
            for (String[] columna : columnas) {
                String sql;
                if (mysql) {
                    sql = "ALTER TABLE " + columna[0] + " AUTO_INCREMENT = " + columna[2];
                } else {
                    sql = "ALTER TABLE " + columna[0] + " ALTER COLUMN "
                            + columna[1] + " RESTART WITH " + columna[2];
                }
                sentencia.execute(sql);
            }
        }
    }
}
