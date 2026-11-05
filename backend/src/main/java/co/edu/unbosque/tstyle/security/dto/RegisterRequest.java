
package co.edu.unbosque.tstyle.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100,
            message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    /**
     * Correo electrónico del cliente.
     */
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no tiene un formato válido")
    @Size(max = 150,
            message = "El correo electrónico no puede superar los 150 caracteres")
    private String email;

    /**
     * Contraseña elegida por el cliente.
     */
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 72,
            message = "La contraseña debe tener entre 8 y 72 caracteres")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9\\s])\\S+$",
            message = "La contraseña debe incluir mayúscula, minúscula, número y símbolo, sin espacios"
    )
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
