package proyecto.clasesTablas;

/** Representa una dirección asociada a uno o varios clientes. */
public class Direccion {
    private Long idDireccion;
    private String calle;
    private String ciudad;
    private String codigoPostal;
    private String pais;

    /** Constructor vacío para facilitar la creación y carga del objeto. */
    public Direccion() {
    }

    /** Constructor con todos los campos. */
    public Direccion(Long idDireccion, String calle, String ciudad, String codigoPostal, String pais) {
        this.idDireccion = idDireccion;
        this.calle = calle;
        this.ciudad = ciudad;
        this.codigoPostal = codigoPostal;
        this.pais = pais;
    }

    public Long getIdDireccion() {
        return idDireccion;
    }

    public void setIdDireccion(Long idDireccion) {
        this.idDireccion = idDireccion;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }
}