package proyecto.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Cliente con su lista de direcciones, que se completa desde el DAO. */
public class ClienteDTO {
    private final int id;
    private final String nombre;
    private final String correo;
    private final List<String> direcciones = new ArrayList<>();

    public ClienteDTO(int id, String nombre, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
    }

    public int id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    public String correo() {
        return correo;
    }

    public List<String> direcciones() {
        return Collections.unmodifiableList(direcciones);
    }

    public void agregarDireccion(String direccion) {
        if (direccion != null) {
            direcciones.add(direccion);
        }
    }
}
