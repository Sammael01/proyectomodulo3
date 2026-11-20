package co.edu.unbosque.tstyle.exception;

/**
 * Representa una excepción generada cuando una operación requiere
 * un usuario autenticado y no se pudo identificar a ninguno.
 *
 * @author Equipo T Style
 */
public class UnauthorizedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor que recibe el mensaje del error.
     *
     * @param message descripción del problema de autenticación
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}
