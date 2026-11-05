
package co.edu.unbosque.tstyle.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no tiene un formato válido")
    @Size(max = 150,
            message = "El correo electrónico no puede superar los 150 caracteres")
    private String email;

    /**
     * Contraseña del usuario.
     */
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 72,
            message = "La contraseña no puede superar los 72 caracteres")
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
     * @param email correo electrónico
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
