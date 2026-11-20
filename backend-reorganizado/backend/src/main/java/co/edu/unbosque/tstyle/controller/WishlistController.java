package co.edu.unbosque.tstyle.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.tstyle.dto.CartResponse;
import co.edu.unbosque.tstyle.dto.QuantityRequest;
import co.edu.unbosque.tstyle.dto.WishlistItemResponse;
import co.edu.unbosque.tstyle.dto.WishlistStatusRequest;
import co.edu.unbosque.tstyle.enums.WishlistStatus;
import co.edu.unbosque.tstyle.security.CurrentUserService;
import co.edu.unbosque.tstyle.service.WishlistService;
import jakarta.validation.Valid;

/**
 * Controlador de la wishlist personal del usuario autenticado.
 *
 * @author Equipo T Style
 */
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final CurrentUserService currentUserService;

    /**
     * Constructor del controlador de la wishlist.
     *
     * @param wishlistService servicio de la wishlist
     * @param currentUserService acceso al usuario autenticado
     */
    public WishlistController(WishlistService wishlistService, CurrentUserService currentUserService) {
        this.wishlistService = wishlistService;
        this.currentUserService = currentUserService;
    }

    /**
     * Lista la wishlist, opcionalmente filtrada por estado.
     *
     * @param status WATCH_LATER, LIKE o DISLIKE (opcional)
     * @return productos de la wishlist
     */
    @GetMapping
    public List<WishlistItemResponse> list(@RequestParam(required = false) WishlistStatus status) {
        return wishlistService.list(currentUserService.requireUser(), status);
    }

    /**
     * Guarda un producto o cambia su estado (no duplica).
     *
     * @param productId producto a guardar
     * @param request estado deseado; si se omite, WATCH_LATER
     * @return elemento guardado
     */
    @PutMapping("/{productId}")
    public WishlistItemResponse save(
            @PathVariable Long productId,
            @RequestBody(required = false) WishlistStatusRequest request) {

        WishlistStatus status = (request == null) ? null : request.status();

        return wishlistService.save(currentUserService.requireUser(), productId, status);
    }

    /**
     * Quita un producto de la wishlist.
     *
     * @param productId producto a quitar
     * @return respuesta sin contenido
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> remove(@PathVariable Long productId) {

        wishlistService.remove(currentUserService.requireUser(), productId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Mueve un producto de la wishlist al carrito.
     *
     * @param productId producto a mover
     * @param request unidades a agregar; si se omite, 1
     * @return resumen actualizado del carrito
     */
    @PostMapping("/{productId}/move-to-cart")
    public CartResponse moveToCart(
            @PathVariable Long productId,
            @Valid @RequestBody(required = false) QuantityRequest request) {

        int quantity = (request == null) ? 1 : request.quantity();

        return wishlistService.moveToCart(currentUserService.requireUser(), productId, quantity);
    }
}
