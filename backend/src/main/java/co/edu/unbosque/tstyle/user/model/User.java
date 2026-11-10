
package co.edu.unbosque.tstyle.user.model;

import java.time.LocalDateTime;

import co.edu.unbosque.tstyle.user.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Representa un usuario registrado en la plataforma T Style.
 *
 * Esta clase define la información de los usuarios que se almacenará
 * en la base de datos.
 *
 * @author Equipo T Style
 */
@Entity
@Table(name = "tstyle_users")
public class User {

    /**
     * Identificador único del usuario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre completo del usuario.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Correo electrónico utilizado para iniciar sesión.
     */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Contraseña almacenada mediante un algoritmo de hash seguro.
     */
    @Column(nullable = false, length = 255)
    private String password;

    /**
     * Rol asignado al usuario dentro de la plataforma.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /**
     * Indica si el usuario ha verificado su correo electrónico.
     */
    @Column(nullable = false)
    private boolean emailVerified;

    /**
     * Indica si la cuenta del usuario está habilitada.
     */
    @Column(nullable = false)
    private boolean enabled;

    /**
     * Fecha y hora de creación del usuario.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Constructor vacío requerido por JPA.
     */
    public User() {
        this.emailVerified = false;
        this.enabled = true;
    }

    /**
     * Constructor con los atributos principales del usuario.
     *
     * @param name nombre completo
     * @param email correo electrónico
     * @param password contraseña cifrada mediante hash
     * @param role rol asignado
     */
    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.emailVerified = false;
        this.enabled = true;
    }

    /**
     * Establece la fecha de creación antes de guardar el usuario.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
