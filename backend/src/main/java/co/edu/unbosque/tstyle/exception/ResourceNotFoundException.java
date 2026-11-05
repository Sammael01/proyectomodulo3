package co.edu.unbosque.tstyle.exception;

/**
 * Representa una excepción generada cuando un recurso
 * solicitado no existe en la plataforma T Style.
 *
 * @author Equipo T Style
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor que recibe el mensaje del error.
     *
     * @param message descripción del recurso no encontrado
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}