package proyecto.dto;

/** Datos de catálogo y, cuando procede, existencias del producto. */
public record ProductoDTO(int id, String nombre, double precio, int stock) {
    public ProductoDTO(int id, String nombre, double precio) {
        this(id, nombre, precio, -1);
    }
}
