package proyecto.service;

import proyecto.clasesTablas.Inventario;
import proyecto.dao.InventarioDAO;
import proyecto.dto.ProductoMasVendidoDTO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Consultas resumidas para los informes de ventas y existencias. */
public class InformeService {
    private final Connection conexion;
    private final InventarioDAO inventarioDAO;

    public InformeService(Connection conexion) {
        this.conexion = conexion;
        inventarioDAO = new InventarioDAO(conexion);
    }

    public List<ProductoMasVendidoDTO> obtenerProductosMasVendidos(int limite) throws SQLException {
        if (limite <= 0) {
            throw new IllegalArgumentException("El límite debe ser positivo");
        }
        String sql = "SELECT p.id_producto,p.nombre,SUM(dp.cantidadProducto) AS unidades_vendidas, "
                + "SUM(dp.cantidadProducto * dp.precio_unitario) AS ingresos_totales "
                + "FROM producto p JOIN detalle_pedido dp ON dp.id_producto=p.id_producto "
                + "JOIN pedido pe ON pe.id_pedido=dp.id_pedido "
                + "WHERE pe.estado='COMPLETADO' "
                + "GROUP BY p.id_producto,p.nombre "
                + "ORDER BY unidades_vendidas DESC LIMIT ?";
        List<ProductoMasVendidoDTO> informe = new ArrayList<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, limite);
            try (ResultSet resultados = sentencia.executeQuery()) {
                while (resultados.next()) {
                    informe.add(new ProductoMasVendidoDTO(resultados.getLong("id_producto"),
                            resultados.getString("nombre"), resultados.getLong("unidades_vendidas"),
                            resultados.getBigDecimal("ingresos_totales")));
                }
            }
        }
        return informe;
    }

    public BigDecimal obtenerIngresosTotales() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total),0) AS ingresos FROM pedido WHERE estado='COMPLETADO'";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultados = sentencia.executeQuery()) {
            resultados.next();
            return resultados.getBigDecimal("ingresos");
        }
    }

    public List<Inventario> obtenerStockBajo() throws SQLException {
        return inventarioDAO.listarStockBajo();
    }
}
