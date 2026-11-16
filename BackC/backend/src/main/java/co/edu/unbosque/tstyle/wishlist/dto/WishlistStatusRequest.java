package co.edu.unbosque.tstyle.wishlist.dto;

import co.edu.unbosque.tstyle.wishlist.enums.WishlistStatus;

/**
 * Estado que el usuario quiere asignar a un producto de su wishlist.
 * Si no se envía, se usa WATCH_LATER ("Ver más tarde").
 *
 * @param status estado deseado
 *
 * @author Equipo T Style
 */
public record WishlistStatusRequest(WishlistStatus status) {
}
