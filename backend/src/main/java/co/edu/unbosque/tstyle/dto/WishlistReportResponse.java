package co.edu.unbosque.tstyle.dto;

public record WishlistReportResponse(
        Long productId,
        String name,
        long timesSaved,
        long likes) {
}
