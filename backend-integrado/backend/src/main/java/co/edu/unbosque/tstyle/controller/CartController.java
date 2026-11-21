package co.edu.unbosque.tstyle.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.tstyle.dto.CartItemRequest;
import co.edu.unbosque.tstyle.dto.CartResponse;
import co.edu.unbosque.tstyle.dto.QuantityRequest;
import co.edu.unbosque.tstyle.security.CurrentUserService;
import co.edu.unbosque.tstyle.service.CartService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Carrito", description = "Gestión del carrito del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;
    private final CurrentUserService currentUserService;

    public CartController(CartService cartService, CurrentUserService currentUserService) {
        this.cartService = cartService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public CartResponse getCart() {
        return cartService.getCart(currentUserService.requireUser());
    }

    @PostMapping("/items")
    public CartResponse addItem(@Valid @RequestBody CartItemRequest request) {
        return cartService.addItem(currentUserService.requireUser(), request.productId(), request.quantity());
    }

    @PutMapping("/items/{productId}")
    public CartResponse updateItem(
            @PathVariable Long productId, @Valid @RequestBody QuantityRequest request) {
        return cartService.updateItem(currentUserService.requireUser(), productId, request.quantity());
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(@PathVariable Long productId) {
        return cartService.removeItem(currentUserService.requireUser(), productId);
    }

    @DeleteMapping
    public CartResponse clear() {
        return cartService.clear(currentUserService.requireUser());
    }
}
