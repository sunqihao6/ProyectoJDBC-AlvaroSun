package proyecto.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proyecto.database.DatabaseSchemaInitializer;
import proyecto.database.InitialDataLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;

/** Coordina la conexión, la creación de tablas y la carga inicial de datos. */
public final class DatabaseInitializer {
    private static final Logger logger = LogManager.getLogger(DatabaseInitializer.class);

    private DatabaseInitializer() {
    }

    public static void initialize(Properties properties) throws SQLException {
        // PASO 1: obtener el motor seleccionado en application.properties.
        String engine = properties.getProperty("database.engine", "H2")
                .trim().toUpperCase(Locale.ROOT);
        boolean mysql = "MYSQL".equals(engine);

        if (!mysql && !"H2".equals(engine)) {
            throw new IllegalArgumentException("Motor no admitido: " + engine);
        }

        // PASO 2: abrir una conexión con el motor elegido.
        try (Connection connection = openConnection(properties, engine)) {
            String databaseName = "unidad2";

            // PASO 3: crear/seleccionar la base MySQL o crear el archivo H2 si hace falta.
            if (mysql) {
                DatabaseSchemaInitializer.crearBaseDatosMySQL(connection, databaseName);
            }

            // PASO 4: crear las tablas solo si todavía no existen.
            DatabaseSchemaInitializer.crearTablas(connection, mysql);

            // PASO 5: restaurar el estado inicial antes de cada simulación.
            InitialDataLoader.restaurarYCargar(connection, mysql);
        }

        // PASO 6: try-with-resources cierra la conexión al terminar.
        logger.info("Inicialización de {} terminada", engine);
    }

    private static Connection openConnection(Properties properties, String engine)
            throws SQLException {
        // MySQL conecta primero al servidor sin seleccionar una base de datos.
        String urlProperty = engine.equals("MYSQL") ? "mysql.serverUrl" : "h2.url";
        String url = recuperarURL(properties, urlProperty);

        String SGBD = engine.toLowerCase(Locale.ROOT);
        String user = properties.getProperty(SGBD + ".user", "");
        String password = properties.getProperty(SGBD + ".password", "");

        logger.info("Conectando a {}", engine);
        return DriverManager.getConnection(url, user, password);
    }

    private static String recuperarURL(Properties properties, String propertyURL) {
        String value = properties.getProperty(propertyURL);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Falta la propiedad obligatoria: " + propertyURL);
        }
        return value.trim();
    }
}
