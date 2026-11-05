package co.edu.unbosque.tstyle.exception;

/**
 * Representa una excepción generada cuando una operación
 * incumple una regla de negocio de la plataforma T Style.
 *
 * @author Equipo T Style
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor que recibe el mensaje del error.
     *
     * @param message descripción del problema
     */
    public BusinessException(String message) {
        super(message);
    }
}