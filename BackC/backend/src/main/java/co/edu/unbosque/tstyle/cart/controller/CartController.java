package co.edu.unbosque.tstyle.cart.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.tstyle.cart.dto.CartItemRequest;
import co.edu.unbosque.tstyle.cart.dto.CartResponse;
import co.edu.unbosque.tstyle.cart.dto.QuantityRequest;
import co.edu.unbosque.tstyle.cart.service.CartService;
import co.edu.unbosque.tstyle.security.CurrentUserService;
import jakarta.validation.Valid;

/**
 * Controlador del carrito de compras del usuario autenticado.
 *
 * @author Equipo T Style
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final CurrentUserService currentUserService;

    /**
     * Constructor del controlador del carrito.
     *
     * @param cartService servicio del carrito
     * @param currentUserService acceso al usuario autenticado
     */
    public CartController(CartService cartService, CurrentUserService currentUserService) {
        this.cartService = cartService;
        this.currentUserService = currentUserService;
    }

    /**
     * Consulta el carrito con su resumen.
     *
     * @return resumen del carrito
     */
    @GetMapping
    public CartResponse getCart() {
        return cartService.getCart(currentUserService.requireUser());
    }

    /**
     * Agrega un producto al carrito (suma si ya existía).
     *
     * @param request producto y cantidad
     * @return resumen actualizado
     */
    @PostMapping("/items")
    public CartResponse addItem(@Valid @RequestBody CartItemRequest request) {
        return cartService.addItem(
                currentUserService.requireUser(), request.productId(), request.quantity());
    }

    /**
     * Cambia la cantidad de un producto del carrito.
     *
     * @param productId producto a modificar
     * @param request nueva cantidad
     * @return resumen actualizado
     */
    @PutMapping("/items/{productId}")
    public CartResponse updateItem(
            @PathVariable Long productId,
            @Valid @RequestBody QuantityRequest request) {

        return cartService.updateItem(
                currentUserService.requireUser(), productId, request.quantity());
    }

    /**
     * Quita un producto del carrito.
     *
     * @param productId producto a quitar
     * @return resumen actualizado
     */
    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(@PathVariable Long productId) {
        return cartService.removeItem(currentUserService.requireUser(), productId);
    }

    /**
     * Vacía el carrito.
     *
     * @return resumen del carrito vacío
     */
    @DeleteMapping
    public CartResponse clear() {
        return cartService.clear(currentUserService.requireUser());
    }
}
