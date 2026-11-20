package co.edu.unbosque.tstyle.dto;

import co.edu.unbosque.tstyle.enums.WishlistStatus;

/**
 * Estado que el usuario asigna a un producto de su wishlist.
 * Si no se envía, se usa WATCH_LATER ("Ver más tarde").
 *
 * @param status estado deseado
 *
 * @author Equipo T Style
 */
public record WishlistStatusRequest(WishlistStatus status) {
}
