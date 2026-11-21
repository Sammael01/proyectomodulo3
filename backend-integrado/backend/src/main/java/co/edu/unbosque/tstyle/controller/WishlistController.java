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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Wishlist", description = "Lista de deseos del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {
    private final WishlistService wishlistService;
    private final CurrentUserService currentUserService;

    public WishlistController(WishlistService wishlistService, CurrentUserService currentUserService) {
        this.wishlistService = wishlistService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<WishlistItemResponse> list(@RequestParam(required = false) WishlistStatus status) {
        return wishlistService.list(currentUserService.requireUser(), status);
    }

    @PutMapping("/{productId}")
    public WishlistItemResponse save(
            @PathVariable Long productId,
            @RequestBody(required = false) WishlistStatusRequest request) {
        WishlistStatus status = (request == null) ? null : request.status();

        return wishlistService.save(currentUserService.requireUser(), productId, status);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> remove(@PathVariable Long productId) {
        wishlistService.remove(currentUserService.requireUser(), productId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/move-to-cart")
    public CartResponse moveToCart(
            @PathVariable Long productId,
            @Valid @RequestBody(required = false) QuantityRequest request) {
        int quantity = (request == null) ? 1 : request.quantity();

        return wishlistService.moveToCart(currentUserService.requireUser(), productId, quantity);
    }
}
