package co.edu.unbosque.tstyle.cart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.tstyle.cart.dto.CartResponse;
import co.edu.unbosque.tstyle.cart.model.CartItem;
import co.edu.unbosque.tstyle.cart.repository.CartItemRepository;
import co.edu.unbosque.tstyle.exception.BusinessException;
import co.edu.unbosque.tstyle.exception.ResourceNotFoundException;
import co.edu.unbosque.tstyle.product.model.Product;
import co.edu.unbosque.tstyle.product.repository.ProductRepository;
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;

/**
 * Pruebas unitarias para CartService.
 *
 * @author Equipo T Style
 */
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User("Ana Pérez", "ana@tstyle.com", "hash", Role.CLIENT);
        user.setId(1L);

        product = new Product("Camisa", new BigDecimal("50000.00"), 5);
        product.setId(10L);
    }

    @Test
    void addItemCreatesNewLineAndCalculatesTotalOnServer() {

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.empty());
        when(cartItemRepository.findAllByUserWithProduct(1L))
                .thenReturn(List.of(new CartItem(user, product, 2)));

        CartResponse response = cartService.addItem(user, 10L, 2);

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());

        assertEquals(2, captor.getValue().getQuantity());
        assertEquals(2, response.totalItems());
        assertEquals(0, new BigDecimal("100000.00").compareTo(response.total()));
    }

    @Test
    void addItemSumsQuantityWhenProductAlreadyInCart() {

        product.setStock(10);
        CartItem existing = new CartItem(user, product, 3);

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.findAllByUserWithProduct(1L)).thenReturn(List.of(existing));

        cartService.addItem(user, 10L, 2);

        // Misma línea, cantidad sumada: no se crea un duplicado.
        verify(cartItemRepository).save(existing);
        assertEquals(5, existing.getQuantity());
    }

    @Test
    void addItemRejectsQuantityAboveStock() {

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> cartService.addItem(user, 10L, 6));

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addItemRejectsMoreThanMaximumPerProduct() {

        product.setStock(100);
        CartItem existing = new CartItem(user, product, 8);

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.of(existing));

        assertThrows(BusinessException.class, () -> cartService.addItem(user, 10L, 5));

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addItemRejectsZeroQuantity() {
        assertThrows(BusinessException.class, () -> cartService.addItem(user, 10L, 0));
    }

    @Test
    void addItemRejectsUnknownProduct() {

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.addItem(user, 99L, 1));
    }

    @Test
    void addItemRejectsInactiveProduct() {

        product.setActive(false);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThrows(ResourceNotFoundException.class, () -> cartService.addItem(user, 10L, 1));
    }

    @Test
    void removeItemFailsWhenProductIsNotInCart() {

        when(cartItemRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.removeItem(user, 10L));
    }
}
