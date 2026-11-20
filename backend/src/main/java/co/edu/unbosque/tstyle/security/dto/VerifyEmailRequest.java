
package co.edu.unbosque.tstyle.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Representa los datos necesarios para verificar el correo electrónico.
 *
 * @author Equipo T Style
 */
public class VerifyEmailRequest {

	@NotBlank(message = "El correo electrónico es obligatorio")
	@Email(message = "El correo electrónico no tiene un formato válido")
	@Size(max = 150, message = "El correo electrónico no puede superar los 150 caracteres")
	private String email;

	@NotBlank(message = "El código de verificación es obligatorio")
	@Pattern(regexp = "^[0-9]{6}$", message = "El código debe contener exactamente seis dígitos")
	private String code;

	public VerifyEmailRequest() {
	}

	public VerifyEmailRequest(String email, String code) {
		this.email = email;
		this.code = code;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
