package proyecto.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Categoría con los productos asociados, completada desde el DAO. */
public class CategoriaDTO {
    private final int id;
    private final String nombre;
    private final List<ProductoDTO> productos = new ArrayList<>();

    public CategoriaDTO(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    public List<ProductoDTO> productos() {
        return Collections.unmodifiableList(productos);
    }

    public void agregarProducto(ProductoDTO producto) {
        productos.add(producto);
    }
}
