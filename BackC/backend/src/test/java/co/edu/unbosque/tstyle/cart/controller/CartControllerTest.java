package co.edu.unbosque.tstyle.cart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unbosque.tstyle.cart.dto.CartLineResponse;
import co.edu.unbosque.tstyle.cart.dto.CartResponse;
import co.edu.unbosque.tstyle.cart.service.CartService;
import co.edu.unbosque.tstyle.exception.UnauthorizedException;
import co.edu.unbosque.tstyle.security.CurrentUserService;
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;

/**
 * Pruebas HTTP para el controlador del carrito.
 *
 * @author Equipo T Style
 */
@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @Test
    void addItemReturnsUpdatedCart() throws Exception {

        User user = new User("Ana Pérez", "ana@tstyle.com", "hash", Role.CLIENT);
        user.setId(1L);

        CartResponse cart = new CartResponse(
                List.of(new CartLineResponse(10L, "Camisa", new BigDecimal("50000.00"), 2,
                        new BigDecimal("100000.00"), 5, true)),
                2,
                new BigDecimal("100000.00"));

        when(currentUserService.requireUser()).thenReturn(user);
        when(cartService.addItem(any(User.class), eq(10L), eq(2))).thenReturn(cart);

        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\": 10, \"quantity\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.items[0].name").value("Camisa"));
    }

    @Test
    void addItemWithInvalidQuantityReturnsValidationError() throws Exception {

        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\": 10, \"quantity\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void getCartWithoutLoginReturnsUnauthorized() throws Exception {

        when(currentUserService.requireUser())
                .thenThrow(new UnauthorizedException("Debes iniciar sesión"));

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }
}
