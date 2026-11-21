package co.edu.unbosque.tstyle.exception;

/**
 * 
 * Indica que un servidor externo no respondio o no esta disponible
 * 
 * @author Sebastián
 */

public class ExternalServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ExternalServiceException(String message) {
		super(message);
	}
}
