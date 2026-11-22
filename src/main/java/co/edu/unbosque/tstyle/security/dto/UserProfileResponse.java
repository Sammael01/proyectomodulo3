
package co.edu.unbosque.tstyle.security.dto;

/**
 * Representa la información del perfil del usuario autenticado.
 *
 * Esta clase permite devolver los datos básicos del usuario sin exponer
 * información sensible ni el token JWT.
 *
 * @author Equipo T Style
 * @version 1.0
 */
public class UserProfileResponse {

	/**
	 * Identificador único del usuario.
	 */
	private Long userId;

	/**
	 * Nombre del usuario.
	 */
	private String name;

	/**
	 * Correo electrónico del usuario.
	 */
	private String email;

	/**
	 * Rol asignado al usuario.
	 */
	private String role;

	/**
	 * Constructor vacío.
	 */
	public UserProfileResponse() {
		super();
	}

	/**
	 * Constructor con todos los atributos del perfil.
	 *
	 * @param userId identificador del usuario
	 * @param name   nombre del usuario
	 * @param email  correo electrónico
	 * @param role   rol asignado
	 */
	public UserProfileResponse(Long userId, String name, String email, String role) {
		super();
		this.userId = userId;
		this.name = name;
		this.email = email;
		this.role = role;
	}

	/**
	 * Obtiene el identificador del usuario.
	 *
	 * @return identificador del usuario
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * Establece el identificador del usuario.
	 *
	 * @param userId identificador del usuario
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * Obtiene el nombre del usuario.
	 *
	 * @return nombre del usuario
	 */
	public String getName() {
		return name;
	}

	/**
	 * Establece el nombre del usuario.
	 *
	 * @param name nombre del usuario
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Obtiene el correo electrónico del usuario.
	 *
	 * @return correo electrónico
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Establece el correo electrónico del usuario.
	 *
	 * @param email correo electrónico
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Obtiene el rol del usuario.
	 *
	 * @return rol asignado
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Establece el rol del usuario.
	 *
	 * @param role rol asignado
	 */
	public void setRole(String role) {
		this.role = role;
	}
}
