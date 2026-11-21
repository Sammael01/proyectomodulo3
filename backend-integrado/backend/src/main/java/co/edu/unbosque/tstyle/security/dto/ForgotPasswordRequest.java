
package co.edu.unbosque.tstyle.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Representa los datos necesarios para solicitar un código de recuperación de
 * contraseña.
 *
 * @author Equipo T Style
 */
public class ForgotPasswordRequest {

	/**
	 * Correo electrónico asociado a la cuenta.
	 */
	@NotBlank(message = "El correo electrónico es obligatorio")
	@Email(message = "El correo electrónico no tiene un formato válido")
	@Size(max = 150, message = "El correo electrónico no puede superar los 150 caracteres")
	private String email;

	/**
	 * Constructor vacío.
	 */
	public ForgotPasswordRequest() {
	}

	/**
	 * Constructor con el correo electrónico.
	 *
	 * @param email correo electrónico del usuario
	 */
	public ForgotPasswordRequest(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
