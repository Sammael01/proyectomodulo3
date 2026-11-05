package co.edu.unbosque.tstyle.security.dto;

/**
 * Representa los datos necesarios para iniciar sesión
 * en la plataforma T Style.
 *
 * @author Equipo T Style
 */
public class LoginRequest {

    /**
     * Correo electrónico del usuario.
     */
    private String email;

    /**
     * Contraseña del usuario.
     */
    private String password;

    /**
     * Constructor vacío.
     */
    public LoginRequest() {
        super();
    }

    /**
     * Constructor con todos los atributos.
     *
     * @param email correo electrónico del usuario
     * @param password contraseña del usuario
     */
    public LoginRequest(String email, String password) {
        super();
        this.email = email;
        this.password = password;
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