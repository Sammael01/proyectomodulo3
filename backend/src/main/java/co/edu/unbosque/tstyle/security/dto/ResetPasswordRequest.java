
package co.edu.unbosque.tstyle.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Representa los datos necesarios para validar un código y establecer una nueva
 * contraseña.
 *
 * @author Equipo T Style
 */
public class ResetPasswordRequest {

	/**
	 * Correo electrónico asociado a la cuenta.
	 */
	@NotBlank(message = "El correo electrónico es obligatorio")
	@Email(message = "El correo electrónico no tiene un formato válido")
	@Size(max = 150, message = "El correo electrónico no puede superar los 150 caracteres")
	private String email;

	/**
	 * Código de recuperación recibido por correo.
	 */
	@NotBlank(message = "El código de recuperación es obligatorio")
	@Pattern(regexp = "^\\d{6}$", message = "El código debe contener seis dígitos")
	private String code;

	/**
	 * Nueva contraseña del usuario.
	 */
	@NotBlank(message = "La nueva contraseña es obligatoria")
	@Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9\\s])\\S+$", message = "La contraseña debe incluir mayúscula, minúscula, número y símbolo, sin espacios")
	private String newPassword;

	/**
	 * Constructor vacío.
	 */
	public ResetPasswordRequest() {
	}

	/**
	 * Constructor con todos los atributos.
	 *
	 * @param email       correo electrónico del usuario
	 * @param code        código de recuperación
	 * @param newPassword nueva contraseña
	 */
	public ResetPasswordRequest(String email, String code, String newPassword) {
		this.email = email;
		this.code = code;
		this.newPassword = newPassword;
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

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
}
