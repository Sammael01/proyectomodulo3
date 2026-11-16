package co.edu.unbosque.tstyle.admin.dto;

/**
 * Producto dentro del reporte de más guardados en wishlists.
 *
 * @param productId identificador del producto
 * @param name nombre del producto
 * @param timesSaved cantidad de wishlists donde aparece
 * @param likes cuántas de esas veces está marcado como "Me gusta"
 *
 * @author Equipo T Style
 */
public record WishlistReportResponse(
        Long productId,
        String name,
        long timesSaved,
        long likes) {
}
