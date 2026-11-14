package proyecto.dto;

import java.math.BigDecimal;

/** Resultado resumido de la consulta de productos más vendidos. */
public class ProductoMasVendidoDTO {
    private final Long idProducto;
    private final String nombreProducto;
    private final Long unidadesVendidas;
    private final BigDecimal ingresosTotales;

    public ProductoMasVendidoDTO(Long idProducto, String nombreProducto,
                                 Long unidadesVendidas, BigDecimal ingresosTotales) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.unidadesVendidas = unidadesVendidas;
        this.ingresosTotales = ingresosTotales;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public Long getUnidadesVendidas() {
        return unidadesVendidas;
    }

    public BigDecimal getIngresosTotales() {
        return ingresosTotales;
    }
}
