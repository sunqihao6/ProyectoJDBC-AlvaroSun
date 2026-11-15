package proyecto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proyecto.clasesTablas.DetallePedido;
import proyecto.clasesTablas.Pedido;
import proyecto.connection.DatabaseInitializer;
import proyecto.dto.CategoriaDTO;
import proyecto.dto.ClienteDTO;
import proyecto.dto.MovimientoDTO;
import proyecto.dto.PedidoDTO;
import proyecto.dto.ProductoDTO;
import proyecto.service.PedidoService;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/** Punto de entrada y presentación de la simulación de Tech Store. */
public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    private static final int PAUSA_ENTRE_PASOS_MS = 1500;

    private Main() {
    }

    public static void main(String[] args) {
        Properties propiedades = new Properties();

        try (InputStream entrada = Main.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            propiedades.load(entrada);

            LOGGER.info("-----------------------------------------------------------------------");
            LOGGER.info("--------------------------------TECH STORE-----------------------------");
            LOGGER.info("-----------------------------------------------------------------------");

            LOGGER.info("PASO 1 - Creación de tablas y registros");
            DatabaseInitializer.initialize(propiedades);
            pausaEntrePasos();

            try (Connection conexion = abrirConexion(propiedades)) {
                ejecutarSimulacion(conexion);
            }
        } catch (IOException | SQLException | RuntimeException error) {
            LOGGER.error("No se pudo ejecutar la aplicación de Tech Store", error);
            System.exit(1);
        }
    }


    private static void ejecutarSimulacion(Connection conexion) throws SQLException {
        Simulacion consultas = new Simulacion(conexion);
        PedidoService servicioPedidos = new PedidoService(conexion);

        mostrarClientes(consultas);
        pausaEntrePasos();

        mostrarCatalogo(consultas);
        pausaEntrePasos();

        mostrarMovimientos(consultas);
        pausaEntrePasos();

        crearPedidosDeEjemplo(consultas, servicioPedidos);
    }


    private static void mostrarClientes(Simulacion consultas) throws SQLException {
        LOGGER.info("PASO 2 - Clientes y direcciones");
        LOGGER.info("CLIENTES:");

        int numeroCliente = 1;
        for (ClienteDTO cliente : consultas.clientesConDirecciones()) {
            LOGGER.info("{}. {} (correo: {})", numeroCliente, cliente.nombre(), cliente.correo());
            numeroCliente++;

            for (String direccion : cliente.direcciones()) {
                LOGGER.info("   Dirección: {}", direccion);
            }
        }
    }


    private static void mostrarCatalogo(Simulacion consultas) throws SQLException {
        LOGGER.info("PASO 3 - Catálogo por categoría y stock disponible");
        LOGGER.info("CATEGORÍAS Y PRODUCTOS:");

        int numeroCategoria = 1;
        for (CategoriaDTO categoria : consultas.productosPorCategoria()) {
            LOGGER.info("{}. {}", numeroCategoria, categoria.nombre());
            numeroCategoria++;

            int numeroProducto = 1;
            for (ProductoDTO producto : categoria.productos()) {
                LOGGER.info("   {}. {} — {} € — stock: {}",
                        numeroProducto, producto.nombre(), producto.precio(), producto.stock());
                numeroProducto++;
            }
        }
    }

    private static void mostrarMovimientos(Simulacion consultas) throws SQLException {
        LOGGER.info("PASO 4 - Historial de movimientos de stock registrados");
        LOGGER.info("MOVIMIENTOS DE PRODUCTOS MODIFICADOS:");

        int numeroMovimiento = 1;
        for (MovimientoDTO movimiento : consultas.historialMovimientos()) {
            LOGGER.info("{}. {} — {} — {}",
                    numeroMovimiento,
                    movimiento.nombreProducto(),
                    movimiento.tipo(),
                    movimiento.fecha());
            numeroMovimiento++;
        }
    }


    private static void crearPedidosDeEjemplo(Simulacion consultas,
                                              PedidoService servicioPedidos) throws SQLException {
        LOGGER.info("PASO 5 - Crear tres pedidos y mostrar sus líneas y pagos");

        int[][] productosPorPedido = {
                {2, 3},
                {8, 9},
                {10, 13}
        };
        int[] clientes = {1, 2, 4};
        String[] metodosPago = {"TARJETA", "EFECTIVO", "TARJETA"};

        for (int indicePedido = 0; indicePedido < productosPorPedido.length; indicePedido++) {
            List<DetallePedido> lineas = crearLineas(productosPorPedido[indicePedido]);
            Pedido pedido = servicioPedidos.realizarPedido(
                    clientes[indicePedido], lineas, metodosPago[indicePedido], "PENDIENTE");

            PedidoDTO detalle = consultas.pedidoCompleto(pedido.getIdPedido());
            mostrarPedido(indicePedido + 1, detalle);
        }
    }

    private static List<DetallePedido> crearLineas(int[] identificadoresProductos) {
        List<DetallePedido> lineas = new ArrayList<>();

        for (int identificadorProducto : identificadoresProductos) {
            DetallePedido linea = new DetallePedido(
                    0, 0, identificadorProducto, 1, 0.0);
            lineas.add(linea);
        }
        return lineas;
    }

    private static void mostrarPedido(int numeroPedido, PedidoDTO pedido) {
        LOGGER.info("{}. Pedido — cliente: {} — correo: {} — dirección: {}",
                numeroPedido,
                pedido.nombreCliente(),
                pedido.emailCliente(),
                pedido.direccion1());
        LOGGER.info("   Productos:");

        for (Map.Entry<ProductoDTO, Integer> entrada : pedido.productos().entrySet()) {
            ProductoDTO producto = entrada.getKey();
            int cantidad = entrada.getValue();
            LOGGER.info("   - {} | precio/unidad: {} € | cantidad: {}",
                    producto.nombre(), producto.precio(), cantidad);
        }

        LOGGER.info("   Precio total: {} €", pedido.precioTotal());
        LOGGER.info("   Pago: {}", pedido.pago());
    }

    private static Connection abrirConexion(Properties propiedades) throws SQLException {
        String motor = propiedades.getProperty("database.engine", "H2")
                .trim()
                .toUpperCase(Locale.ROOT);
        String prefijo = motor.equals("MYSQL") ? "mysql" : "h2";
        String claveUrl = prefijo + ".url";
        String url = propiedades.getProperty(claveUrl);

        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Falta la URL de conexión: " + claveUrl);
        }

        String usuario = propiedades.getProperty(prefijo + ".user", "");
        String contrasena = propiedades.getProperty(prefijo + ".password", "");
        return DriverManager.getConnection(url.trim(), usuario, contrasena);
    }

    private static void pausaEntrePasos() {
        try {
            Thread.sleep(PAUSA_ENTRE_PASOS_MS);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Simulación interrumpida", error);
        }
    }
}
