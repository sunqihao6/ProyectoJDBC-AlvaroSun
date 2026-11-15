package proyecto.clasesTablas;

public class Cliente {

    private int idCliente;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;
    public Cliente(int idCliente, String nombre, String apellidos, String email, String telefono) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
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

    public String getEmail() {
        return email;
    }
}
