package co.edu.unbosque.tstyle.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

import co.edu.unbosque.tstyle.cart.model.CartItem;
import co.edu.unbosque.tstyle.cart.repository.CartItemRepository;
import co.edu.unbosque.tstyle.exception.BusinessException;
import co.edu.unbosque.tstyle.exception.ResourceNotFoundException;
import co.edu.unbosque.tstyle.order.dto.OrderDetailResponse;
import co.edu.unbosque.tstyle.order.enums.EmailStatus;
import co.edu.unbosque.tstyle.order.enums.OrderStatus;
import co.edu.unbosque.tstyle.order.model.PurchaseOrder;
import co.edu.unbosque.tstyle.order.repository.PurchaseOrderRepository;
import co.edu.unbosque.tstyle.product.model.Product;
import co.edu.unbosque.tstyle.product.repository.ProductRepository;
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;

/**
 * Pruebas unitarias para OrderService.
 *
 * @author Equipo T Style
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private PurchaseOrderRepository orderRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product shirt;
    private Product pants;

    @BeforeEach
    void setUp() {
        user = new User("Ana Pérez", "ana@tstyle.com", "hash", Role.CLIENT);
        user.setId(1L);

        shirt = new Product("Camisa", new BigDecimal("50000.00"), 5);
        shirt.setId(10L);

        pants = new Product("Pantalón", new BigDecimal("80000.50"), 5);
        pants.setId(11L);
    }

    @Test
    void checkoutCreatesOrderWithAppliedPricesAndClearsCart() {

        // Desordenado a propósito: el servicio debe procesarlo por id de producto.
        List<CartItem> cart = List.of(new CartItem(user, pants, 1), new CartItem(user, shirt, 2));

        when(cartItemRepository.findAllByUserForUpdate(1L)).thenReturn(cart);
        when(productRepository.decreaseStock(10L, 2)).thenReturn(1);
        when(productRepository.decreaseStock(11L, 1)).thenReturn(1);

        OrderDetailResponse result = orderService.checkout(user);

        ArgumentCaptor<PurchaseOrder> captor = ArgumentCaptor.forClass(PurchaseOrder.class);
        verify(orderRepository).save(captor.capture());
        PurchaseOrder saved = captor.getValue();

        assertEquals(OrderStatus.PAID, saved.getStatus());
        assertEquals(EmailStatus.PENDING, saved.getEmailStatus());
        assertEquals(2, saved.getItems().size());
        assertEquals(10L, saved.getItems().get(0).getProductId());
        assertEquals(0, new BigDecimal("50000.00").compareTo(saved.getItems().get(0).getUnitPrice()));
        assertEquals(0, new BigDecimal("100000.00").compareTo(saved.getItems().get(0).getSubtotal()));
        assertEquals(0, new BigDecimal("180000.50").compareTo(saved.getTotal()));

        verify(cartItemRepository).deleteAll(anyList());
        assertEquals(2, result.items().size());
    }

    @Test
    void checkoutFailsWhenCartIsEmpty() {

        when(cartItemRepository.findAllByUserForUpdate(1L)).thenReturn(List.of());

        assertThrows(BusinessException.class, () -> orderService.checkout(user));

        verify(orderRepository, never()).save(any(PurchaseOrder.class));
        verifyNoInteractions(productRepository);
    }

    @Test
    void checkoutFailsWhenStockIsInsufficient() {

        when(cartItemRepository.findAllByUserForUpdate(1L))
                .thenReturn(List.of(new CartItem(user, shirt, 2)));
        when(productRepository.decreaseStock(10L, 2)).thenReturn(0);

        assertThrows(BusinessException.class, () -> orderService.checkout(user));

        verify(orderRepository, never()).save(any(PurchaseOrder.class));
        verify(cartItemRepository, never()).deleteAll(anyList());
    }

    @Test
    void checkoutFailsWhenProductIsInactive() {

        shirt.setActive(false);

        when(cartItemRepository.findAllByUserForUpdate(1L))
                .thenReturn(List.of(new CartItem(user, shirt, 1)));

        assertThrows(BusinessException.class, () -> orderService.checkout(user));

        verify(productRepository, never()).decreaseStock(anyLong(), anyInt());
        verify(orderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void getOrderHidesOrdersOfOtherUsers() {

        User other = new User("Otro", "otro@tstyle.com", "hash", Role.CLIENT);
        other.setId(99L);

        when(orderRepository.findWithDetailsById(5L))
                .thenReturn(Optional.of(new PurchaseOrder(other)));

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrder(user, 5L));
    }

    @Test
    void getOrderAllowsAdministratorToSeeAnyOrder() {

        User other = new User("Otro", "otro@tstyle.com", "hash", Role.CLIENT);
        other.setId(99L);

        User admin = new User("Admin", "admin@tstyle.com", "hash", Role.ADMIN);
        admin.setId(2L);

        when(orderRepository.findWithDetailsById(5L))
                .thenReturn(Optional.of(new PurchaseOrder(other)));

        OrderDetailResponse response = orderService.getOrder(admin, 5L);

        assertEquals(99L, response.userId());
    }
}
