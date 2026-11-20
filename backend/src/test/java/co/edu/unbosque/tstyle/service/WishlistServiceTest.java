package co.edu.unbosque.tstyle.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.tstyle.dto.CartResponse;
import co.edu.unbosque.tstyle.dto.WishlistItemResponse;
import co.edu.unbosque.tstyle.enums.Role;
import co.edu.unbosque.tstyle.enums.WishlistStatus;
import co.edu.unbosque.tstyle.exception.BusinessException;
import co.edu.unbosque.tstyle.exception.ResourceNotFoundException;
import co.edu.unbosque.tstyle.model.Product;
import co.edu.unbosque.tstyle.model.User;
import co.edu.unbosque.tstyle.model.WishlistItem;
import co.edu.unbosque.tstyle.repository.ProductRepository;
import co.edu.unbosque.tstyle.repository.WishlistItemRepository;

/**
 * Pruebas unitarias para WishlistService.
 *
 * @author Equipo T Style
 */
@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistItemRepository wishlistItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private WishlistService wishlistService;

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
    void saveCreatesItemWithWatchLaterByDefault() {

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(wishlistItemRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.empty());
        when(wishlistItemRepository.saveAndFlush(any(WishlistItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WishlistItemResponse response = wishlistService.save(user, 10L, null);

        assertEquals(WishlistStatus.WATCH_LATER, response.status());
        assertEquals(10L, response.productId());
    }

    @Test
    void saveExistingProductUpdatesStatusInsteadOfDuplicating() {

        WishlistItem existing = new WishlistItem(user, product, WishlistStatus.LIKE);

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(wishlistItemRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.of(existing));
        when(wishlistItemRepository.saveAndFlush(any(WishlistItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        wishlistService.save(user, 10L, WishlistStatus.DISLIKE);

        // Se reutiliza el mismo registro: un único guardado y con el estado nuevo.
        verify(wishlistItemRepository, times(1)).saveAndFlush(existing);
        assertEquals(WishlistStatus.DISLIKE, existing.getStatus());
    }

    @Test
    void saveFailsWhenProductDoesNotExist() {

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> wishlistService.save(user, 99L, null));
    }

    @Test
    void moveToCartAddsToCartAndRemovesFromWishlist() {

        WishlistItem item = new WishlistItem(user, product, WishlistStatus.WATCH_LATER);
        CartResponse cart = new CartResponse(List.of(), 1, BigDecimal.ZERO);

        when(wishlistItemRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.of(item));
        when(cartService.addItem(user, 10L, 1)).thenReturn(cart);

        CartResponse result = wishlistService.moveToCart(user, 10L, 1);

        assertEquals(cart, result);
        verify(wishlistItemRepository).delete(item);
    }

    @Test
    void moveToCartKeepsItemInWishlistWhenCartRejectsIt() {

        WishlistItem item = new WishlistItem(user, product, WishlistStatus.WATCH_LATER);

        when(wishlistItemRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.of(item));
        when(cartService.addItem(user, 10L, 1)).thenThrow(new BusinessException("Sin stock"));

        assertThrows(BusinessException.class, () -> wishlistService.moveToCart(user, 10L, 1));

        verify(wishlistItemRepository, never()).delete(any(WishlistItem.class));
    }
}
