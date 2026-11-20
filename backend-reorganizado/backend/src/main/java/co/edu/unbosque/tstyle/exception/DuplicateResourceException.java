package co.edu.unbosque.tstyle.exception;

/**
 * Representa una excepción generada cuando se intenta
 * registrar un recurso que ya existe en T Style.
 *
 * @author Equipo T Style
 */
public class DuplicateResourceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor que recibe el mensaje del error.
     *
     * @param message descripción del recurso duplicado
     */
    public DuplicateResourceException(String message) {
        super(message);
    }
}