package proyecto.dto;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Pedido listo para presentar, con el detalle de productos y el estado del pago. */
public record PedidoDTO(int id, String nombreCliente, String emailCliente, String direccion1,
                        Map<ProductoDTO, Integer> productos, double precioTotal, String pago) {
    public PedidoDTO {
        productos = Collections.unmodifiableMap(new LinkedHashMap<>(productos));
    }
}
