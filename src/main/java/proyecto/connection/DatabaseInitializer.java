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

//Crea tablas y carga datos iniciales
public final class DatabaseInitializer {
    private static final Logger logger = LogManager.getLogger(DatabaseInitializer.class);

    public static void initialize(Properties properties) throws SQLException {
        // Se obtiene el motor seleccionado
        String engine = properties.getProperty("database.engine", "H2")
                .trim().toUpperCase(Locale.ROOT);
        boolean mysql = "MYSQL".equals(engine);

        if (!mysql && !"H2".equals(engine)) {
            throw new IllegalArgumentException("Motor no admitido: " + engine);
        }

        // Se abre una conexion con el motor seleccionado
        try (Connection connection = openConnection(properties, engine)) {
            String databaseName = "unidad2";

            // creaa la BBDD en MySQL
            if (mysql) {
                DatabaseSchemaInitializer.crearBaseDatosMySQL(connection, databaseName);
            }

            // crear las tablas
            DatabaseSchemaInitializer.crearTablas(connection, mysql);

            // cargar datos iniciales
            InitialDataLoader.restaurarYCargar(connection, mysql);
        }
        logger.info("Inicialización de {} exitosa", engine);
    }

    private static Connection openConnection(Properties properties, String engine)
            throws SQLException {

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
