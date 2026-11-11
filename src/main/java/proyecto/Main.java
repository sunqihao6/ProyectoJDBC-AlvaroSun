package proyecto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proyecto.connection.DatabaseInitializer;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        // PASO 1: crear el objeto que almacenará application.properties.
        Properties properties = new Properties();

        // PASO 2: abrir el archivo de configuración incluido en resources.
        try (InputStream input = Main.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IOException("No se encuentra application.properties");
            }

            // PASO 3: cargar las propiedades del motor y la conexión.
            properties.load(input);

            // PASO 4: crear el esquema y cargar los datos iniciales.
            DatabaseInitializer.initialize(properties);
        } catch (IOException | SQLException | IllegalArgumentException e) {
            // PASO 5: registrar el error si la configuración o la BBDD falla.
            logger.error("No se pudo inicializar la base de datos de Tech Store", e);
            System.exit(1);
        }
    }
}
