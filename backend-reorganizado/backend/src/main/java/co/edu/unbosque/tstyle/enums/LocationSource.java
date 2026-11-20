package co.edu.unbosque.tstyle.enums;

/**
 * Origen de la ubicación de un evento de auditoría. La IP solo da una
 * ubicación aproximada; las coordenadas exigen el permiso del usuario.
 *
 * @author Equipo T Style
 */
public enum LocationSource {

    /** Solo se conoce la dirección IP (sin coordenadas). */
    IP,

    /** Coordenadas enviadas por el navegador con permiso del usuario. */
    GPS
}
