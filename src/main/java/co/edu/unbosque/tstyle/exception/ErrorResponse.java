package co.edu.unbosque.tstyle.exception;

import java.time.LocalDateTime;

/**
 * Representa una respuesta estandarizada de error generada por el backend de T
 * Style.
 *
 * @author Equipo T Style
 */
public class ErrorResponse {

	private LocalDateTime timestamp;
	private int status;
	private String error;
	private String message;
	private String path;

	/**
	 * Constructor vacío.
	 */
	public ErrorResponse() {
		super();
	}

	/**
	 * Constructor con todos los atributos.
	 *
	 * @param timestamp fecha y hora del error
	 * @param status    código HTTP
	 * @param error     tipo de error
	 * @param message   descripción del error
	 * @param path      endpoint donde ocurrió el error
	 */
	public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
		super();
		this.timestamp = timestamp;
		this.status = status;
		this.error = error;
		this.message = message;
		this.path = path;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}