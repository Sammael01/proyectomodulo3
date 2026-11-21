
package co.edu.unbosque.tstyle.audit.enums;

/**
 * Define los tipos de eventos que pueden registrarse en el sistema de auditoría
 * de T Style.
 *
 * @author Equipo T Style
 * @version 1.0
 */
public enum AuditEventType {

	// Registro y verificación de usuarios
	USER_REGISTERED, EMAIL_VERIFICATION_SUCCESS, EMAIL_VERIFICATION_FAILURE, VERIFICATION_CODE_RESENT,

	// Autenticación
	LOGIN_SUCCESS, LOGIN_FAILURE,

	// Recuperación de contraseña
	PASSWORD_RESET_REQUEST, PASSWORD_RESET_SUCCESS, PASSWORD_RESET_FAILURE,

	// Administración y seguridad
	USER_UPDATED, USER_DISABLED, USER_ENABLED, ROLE_CHANGED,

	ORDER_CREATED, ORDER_STATUS_CHANGED
}
