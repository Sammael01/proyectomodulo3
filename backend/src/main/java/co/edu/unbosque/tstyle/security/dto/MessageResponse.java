package co.edu.unbosque.tstyle.security.dto;

/**
 * Representa un mensaje de respuesta enviado
 * por el backend al cliente.
 *
 * @author Equipo T Style
 */
public class MessageResponse {

    /**
     * Mensaje que se enviará al cliente.
     */
    private String message;

    /**
     * Constructor vacío.
     */
    public MessageResponse() {
        super();
    }

    /**
     * Constructor con el mensaje.
     *
     * @param message contenido del mensaje
     */
    public MessageResponse(String message) {
        super();
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}