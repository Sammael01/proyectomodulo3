package co.edu.unbosque.tstyle.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.tstyle.exception.BusinessException;
import co.edu.unbosque.tstyle.exception.ResourceNotFoundException;
import co.edu.unbosque.tstyle.order.dto.OrderDetailResponse;
import co.edu.unbosque.tstyle.order.enums.OrderStatus;
import co.edu.unbosque.tstyle.order.model.OrderItem;
import co.edu.unbosque.tstyle.order.model.PurchaseOrder;
import co.edu.unbosque.tstyle.order.repository.PurchaseOrderRepository;
import co.edu.unbosque.tstyle.product.repository.ProductRepository;
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;

/**
 * Pruebas unitarias para AdminOrderService.
 *
 * @author Equipo T Style
 */
@ExtendWith(MockitoExtension.class)
class AdminOrderServiceTest {

    @Mock
    private PurchaseOrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private AdminOrderService adminOrderService;

    private PurchaseOrder order;

    @BeforeEach
    void setUp() {
        User buyer = new User("Ana Pérez", "ana@tstyle.com", "hash", Role.CLIENT);
        buyer.setId(5L);

        order = new PurchaseOrder(buyer);
        order.addItem(new OrderItem(order, 10L, "Camisa", new BigDecimal("50000.00"), 2));
    }

    @Test
    void paidOrderCanBeShipped() {

        when(orderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(order));

        OrderDetailResponse response = adminOrderService.updateStatus(1L, OrderStatus.SHIPPED);

        assertEquals(OrderStatus.SHIPPED, response.status());
        verify(productRepository, never()).increaseStock(anyLong(), anyInt());
    }

    @Test
    void cancellingAnOrderReturnsStockToInventory() {

        when(orderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(order));

        OrderDetailResponse response = adminOrderService.updateStatus(1L, OrderStatus.CANCELLED);

        assertEquals(OrderStatus.CANCELLED, response.status());
        verify(productRepository).increaseStock(10L, 2);
    }

    @Test
    void deliveredOrderCannotChangeStatusAgain() {

        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class,
                () -> adminOrderService.updateStatus(1L, OrderStatus.PAID));

        verify(productRepository, never()).increaseStock(anyLong(), anyInt());
    }

    @Test
    void shippedOrderCannotBeCancelled() {

        order.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class,
                () -> adminOrderService.updateStatus(1L, OrderStatus.CANCELLED));

        verify(productRepository, never()).increaseStock(anyLong(), anyInt());
    }

    @Test
    void updateStatusFailsWhenOrderDoesNotExist() {

        when(orderRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> adminOrderService.updateStatus(99L, OrderStatus.SHIPPED));
    }
}
