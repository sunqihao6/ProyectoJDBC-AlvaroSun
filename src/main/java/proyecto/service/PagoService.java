package proyecto.service;

import proyecto.clasesTablas.Pago;
import proyecto.dao.PagoDAO;

import java.sql.Connection;
import java.sql.SQLException;

/** Reglas de consulta y actualización de pagos simulados. */
public class PagoService {
    private final PagoDAO pagoDAO;

    public PagoService(Connection conexion) {
        pagoDAO = new PagoDAO(conexion);
    }

    public Pago consultarPagoPorPedido(long idPedido) throws SQLException {
        return pagoDAO.buscarPorPedido(idPedido);
    }

    public boolean actualizarEstado(long idPago, String nuevoEstado) throws SQLException {
        if (!"PENDIENTE".equals(nuevoEstado) && !"COMPLETADO".equals(nuevoEstado)
                && !"ANULADO".equals(nuevoEstado)) {
            throw new IllegalArgumentException("Estado de pago no admitido: " + nuevoEstado);
        }
        return pagoDAO.actualizarEstado(idPago, nuevoEstado);
    }
}
