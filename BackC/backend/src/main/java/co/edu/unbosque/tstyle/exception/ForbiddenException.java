package co.edu.unbosque.tstyle.exception;

/**
 * Representa una excepción generada cuando el usuario está autenticado
 * pero no tiene permisos para realizar la operación.
 *
 * @author Equipo T Style
 */
public class ForbiddenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor que recibe el mensaje del error.
     *
     * @param message descripción del permiso que falta
     */
    public ForbiddenException(String message) {
        super(message);
    }
}
