
package co.edu.unbosque.tstyle.user.model;

/**
 * Representa un usuario registrado en la plataforma T Style.
 *
 * Contiene la información básica necesaria para identificar al usuario
 * y gestionar su acceso a la aplicación.
 *
 * @author Equipo T Style
 */
public class User {

    /**
     * Identificador único del usuario.
     */
    private Long id;

    /**
     * Nombre completo del usuario.
     */
    private String name;

    /**
     * Correo electrónico utilizado para iniciar sesión.
     */
    private String email;

    /**
     * Contraseña almacenada mediante un algoritmo de hash seguro.
     */
    private String password;

    /**
     * Rol asignado al usuario dentro de la plataforma.
     */
    private String role;

    /**
     * Indica si el usuario ha verificado su correo electrónico.
     */
    private Boolean emailVerified;

    /**
     * Constructor vacío de la clase User.
     */
    public User() {
        super();
    }

    /**
     * Constructor con todos los atributos del usuario.
     */
    public User(Long id, String name, String email, String password,
                String role, Boolean emailVerified) {
        super();
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.emailVerified = emailVerified;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
}
