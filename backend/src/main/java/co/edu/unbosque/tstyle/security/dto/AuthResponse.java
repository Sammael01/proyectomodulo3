package co.edu.unbosque.tstyle.security.dto;

/**
 * Representa la respuesta que recibe el cliente
 * después de completar un proceso de autenticación.
 *
 * @author Equipo T Style
 */
public class AuthResponse {

    /**
     * Token de autenticación del usuario.
     */
    private String token;

    /**
     * Identificador del usuario autenticado.
     */
    private Long userId;

    /**
     * Nombre del usuario autenticado.
     */
    private String name;

    /**
     * Correo electrónico del usuario autenticado.
     */
    private String email;

    /**
     * Rol asignado al usuario.
     */
    private String role;

    /**
     * Constructor vacío.
     */
    public AuthResponse() {
        super();
    }

    /**
     * Constructor con todos los atributos.
     *
     * @param token token de autenticación
     * @param userId identificador del usuario
     * @param name nombre del usuario
     * @param email correo electrónico
     * @param role rol del usuario
     */
    public AuthResponse(String token, Long userId, String name,
                        String email, String role) {
        super();
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}