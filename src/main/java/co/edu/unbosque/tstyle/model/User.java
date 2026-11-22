
package co.edu.unbosque.tstyle.model;

import java.time.LocalDateTime;

import co.edu.unbosque.tstyle.enums.Role;
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
 * en la base de datos, incluyendo los datos de verificación,
 * recuperación y control de versiones de autenticación.
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
     * Código de verificación almacenado mediante hash.
     */
    @Column(length = 255)
    private String verificationCodeHash;

    /**
     * Fecha y hora de vencimiento del código de verificación.
     */
    private LocalDateTime verificationExpiresAt;

    /**
     * Número de intentos de verificación realizados.
     */
    @Column(nullable = false)
    private int verificationAttempts;

    /**
     * Fecha y hora del último envío del código de verificación.
     */
    private LocalDateTime lastVerificationSentAt;

    /**
     * Código de recuperación de contraseña almacenado mediante hash.
     */
    @Column(length = 255)
    private String resetCodeHash;

    /**
     * Fecha y hora de vencimiento del código de recuperación.
     */
    private LocalDateTime resetExpiresAt;

    /**
     * Número de intentos de validación del código de recuperación.
     */
    @Column(nullable = false)
    private int resetAttempts;

    /**
     * Fecha y hora del último envío del código de recuperación.
     */
    private LocalDateTime lastResetSentAt;

    /**
     * Fecha y hora del último cambio de contraseña.
     */
    private LocalDateTime passwordChangedAt;

    /**
     * Versión actual de autenticación del usuario.
     */
    @Column(nullable = false)
    private int tokenVersion;

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
        this.enabled = false;
        this.verificationAttempts = 0;
        this.resetAttempts = 0;
        this.tokenVersion = 0;
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
        this.enabled = false;
        this.verificationAttempts = 0;
        this.resetAttempts = 0;
        this.tokenVersion = 0;
    }

    /**
     * Establece la fecha de creación antes de guardar el usuario.
     */
    @PrePersist
	public void onCreate() {
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

    public String getVerificationCodeHash() {
        return verificationCodeHash;
    }

    public void setVerificationCodeHash(String verificationCodeHash) {
        this.verificationCodeHash = verificationCodeHash;
    }

    public LocalDateTime getVerificationExpiresAt() {
        return verificationExpiresAt;
    }

    public void setVerificationExpiresAt(LocalDateTime verificationExpiresAt) {
        this.verificationExpiresAt = verificationExpiresAt;
    }

    public int getVerificationAttempts() {
        return verificationAttempts;
    }

    public void setVerificationAttempts(int verificationAttempts) {
        this.verificationAttempts = verificationAttempts;
    }

    public LocalDateTime getLastVerificationSentAt() {
        return lastVerificationSentAt;
    }

    public void setLastVerificationSentAt(LocalDateTime lastVerificationSentAt) {
        this.lastVerificationSentAt = lastVerificationSentAt;
    }

    public String getResetCodeHash() {
        return resetCodeHash;
    }

    public void setResetCodeHash(String resetCodeHash) {
        this.resetCodeHash = resetCodeHash;
    }

    public LocalDateTime getResetExpiresAt() {
        return resetExpiresAt;
    }

    public void setResetExpiresAt(LocalDateTime resetExpiresAt) {
        this.resetExpiresAt = resetExpiresAt;
    }

    public int getResetAttempts() {
        return resetAttempts;
    }

    public void setResetAttempts(int resetAttempts) {
        this.resetAttempts = resetAttempts;
    }

    public LocalDateTime getLastResetSentAt() {
        return lastResetSentAt;
    }

    public void setLastResetSentAt(LocalDateTime lastResetSentAt) {
        this.lastResetSentAt = lastResetSentAt;
    }

    public LocalDateTime getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }

    public void setTokenVersion(int tokenVersion) {
        this.tokenVersion = tokenVersion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
