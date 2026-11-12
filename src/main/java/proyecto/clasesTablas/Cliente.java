package proyecto.clasesTablas;

/** Representa un cliente registrado en la tienda. */
public class Cliente {
    private Long idCliente;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;

    /** Constructor vacío para facilitar la creación y carga del objeto. */
    public Cliente() {
    }

    /** Constructor con todos los campos. */
    public Cliente(Long idCliente, String nombre, String apellidos, String email, String telefono) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}