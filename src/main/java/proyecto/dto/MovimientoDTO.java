package proyecto.dto;

/** Movimiento de stock con fecha ya preparada para mostrarse. */
public record MovimientoDTO(
        int id,
        String nombreProducto,
        String tipo,
        String fecha) {
}
