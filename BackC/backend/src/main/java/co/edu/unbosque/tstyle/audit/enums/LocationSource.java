package co.edu.unbosque.tstyle.audit.enums;

/**
 * Origen de la información de ubicación de un evento de auditoría.
 *
 * <p>Son cosas distintas y no deben confundirse: la IP solo permite una
 * ubicación aproximada, mientras que las coordenadas del navegador exigen
 * el permiso del usuario.</p>
 *
 * @author Equipo T Style
 */
public enum LocationSource {

    /** Solo se conoce la dirección IP (sin coordenadas). */
    IP,

    /** Coordenadas enviadas por el navegador con permiso del usuario. */
    GPS
}
