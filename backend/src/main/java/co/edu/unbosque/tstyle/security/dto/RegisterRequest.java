
package co.edu.unbosque.tstyle.security.dto;

/**
 * Representa los datos necesarios para registrar
 * un nuevo cliente en la plataforma T Style.
 *
 * @author Equipo T Style
 */
public class RegisterRequest {

    /**
     * Nombre completo del cliente.
     */
    private String name;

    /**
     * Correo electrónico del cliente.
     */
    private String email;

    /**
     * Contraseña elegida por el cliente.
     */
    private String password;

    /**
     * Constructor vacío.
     */
    public RegisterRequest() {
        super();
    }

    /**
     * Constructor con todos los atributos.
     *
     * @param name nombre completo del cliente
     * @param email correo electrónico
     * @param password contraseña elegida
     */
    public RegisterRequest(String name, String email, String password) {
        super();
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
