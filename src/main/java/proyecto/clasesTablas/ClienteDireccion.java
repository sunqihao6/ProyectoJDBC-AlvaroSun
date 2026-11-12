package proyecto.clasesTablas;

/** Representa la relación entre un cliente y una dirección. */
public class ClienteDireccion {
    private Long idCliente;
    private Long idDireccion;

    /** Constructor vacío para facilitar la creación y carga del objeto. */
    public ClienteDireccion() {
    }

    /** Constructor con todos los campos. */
    public ClienteDireccion(Long idCliente, Long idDireccion) {
        this.idCliente = idCliente;
        this.idDireccion = idDireccion;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public Long getIdDireccion() {
        return idDireccion;
    }

    public void setIdDireccion(Long idDireccion) {
        this.idDireccion = idDireccion;
    }
}