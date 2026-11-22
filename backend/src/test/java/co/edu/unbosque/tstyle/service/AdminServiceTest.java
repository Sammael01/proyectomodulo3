package co.edu.unbosque.tstyle.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import co.edu.unbosque.tstyle.dto.OrderResponse;
import co.edu.unbosque.tstyle.dto.PageResponse;
import co.edu.unbosque.tstyle.dto.SalesReportResponse;
import co.edu.unbosque.tstyle.enums.EmailStatus;
import co.edu.unbosque.tstyle.enums.OrderStatus;
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.exception.BusinessException;
import co.edu.unbosque.tstyle.exception.ResourceNotFoundException;
import co.edu.unbosque.tstyle.model.OrderItem;
import co.edu.unbosque.tstyle.model.PurchaseOrder;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.repository.OrderItemRepository;
import co.edu.unbosque.tstyle.repository.ProductRepository;
import co.edu.unbosque.tstyle.repository.PurchaseOrderRepository;
import co.edu.unbosque.tstyle.repository.WishlistItemRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
    @Mock
    private PurchaseOrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private WishlistItemRepository wishlistItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private AdminService adminService;

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

        OrderResponse response = adminService.updateStatus(1L, OrderStatus.SHIPPED);

        assertEquals(OrderStatus.SHIPPED, response.status());
        verify(productRepository, never()).increaseStock(anyLong(), anyInt());
    }

    @Test
    void cancellingAnOrderReturnsStockToInventory() {
        when(orderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(order));

        OrderResponse response = adminService.updateStatus(1L, OrderStatus.CANCELLED);

        assertEquals(OrderStatus.CANCELLED, response.status());
        verify(productRepository).increaseStock(10L, 2);
    }

    @Test
    void deliveredOrderCannotChangeStatusAgain() {
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> adminService.updateStatus(1L, OrderStatus.PAID));

        verify(productRepository, never()).increaseStock(anyLong(), anyInt());
    }

    @Test
    void shippedOrderCannotBeCancelled() {
        order.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> adminService.updateStatus(1L, OrderStatus.CANCELLED));

        verify(productRepository, never()).increaseStock(anyLong(), anyInt());
    }

    @Test
    void updateStatusFailsWhenOrderDoesNotExist() {
        when(orderRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> adminService.updateStatus(99L, OrderStatus.SHIPPED));
    }

    @Test
    void listOrdersFiltersByEmailStatus() {
        when(orderRepository.findByEmailStatus(eq(EmailStatus.FAILED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(order)));

        PageResponse<OrderResponse> result =
                adminService.listOrders(null, EmailStatus.FAILED, PageRequest.of(0, 20));

        assertEquals(1, result.content().size());
        assertEquals(1L, result.totalElements());
    }

    @Test
    void salesReportAddsUpOrdersAndAveragesTheTicket() {
        PurchaseOrder second = new PurchaseOrder(order.getUser());
        order.setTotal(new BigDecimal("100000.00"));
        second.setTotal(new BigDecimal("50000.00"));

        when(orderRepository.findForSalesReport(
                eq(OrderStatus.CANCELLED), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(order, second));

        SalesReportResponse report =
                adminService.salesReport(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

        assertEquals(2, report.orderCount());
        assertEquals(0, new BigDecimal("150000.00").compareTo(report.totalSales()));
        assertEquals(0, new BigDecimal("75000.00").compareTo(report.averageTicket()));
    }

    @Test
    void salesReportRejectsInvertedDateRange() {
        assertThrows(BusinessException.class,
                () -> adminService.salesReport(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 1)));
    }
}
