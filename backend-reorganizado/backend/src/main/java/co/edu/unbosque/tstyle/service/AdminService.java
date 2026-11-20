package co.edu.unbosque.tstyle.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.dto.OrderResponse;
import co.edu.unbosque.tstyle.dto.PageResponse;
import co.edu.unbosque.tstyle.dto.SalesReportResponse;
import co.edu.unbosque.tstyle.dto.TopProductResponse;
import co.edu.unbosque.tstyle.dto.WishlistReportResponse;
import co.edu.unbosque.tstyle.enums.EmailStatus;
import co.edu.unbosque.tstyle.enums.OrderStatus;
import co.edu.unbosque.tstyle.enums.WishlistStatus;
import co.edu.unbosque.tstyle.exception.BusinessException;
import co.edu.unbosque.tstyle.exception.ResourceNotFoundException;
import co.edu.unbosque.tstyle.model.OrderItem;
import co.edu.unbosque.tstyle.model.PurchaseOrder;
import co.edu.unbosque.tstyle.repository.OrderItemRepository;
import co.edu.unbosque.tstyle.repository.ProductRepository;
import co.edu.unbosque.tstyle.repository.PurchaseOrderRepository;
import co.edu.unbosque.tstyle.repository.WishlistItemRepository;

/**
 * Lógica de administración: pedidos y reportes. Los pedidos cancelados no
 * cuentan como ventas. Los reportes concretos deben confirmarse con el equipo.
 *
 * @author Equipo T Style
 */
@Service
public class AdminService {

    private static final int MAX_REPORT_ROWS = 50;

    private final PurchaseOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;

    /**
     * Constructor del servicio de administración.
     *
     * @param orderRepository repositorio de pedidos
     * @param orderItemRepository repositorio del detalle de pedidos
     * @param wishlistItemRepository repositorio de la wishlist
     * @param productRepository repositorio de productos
     */
    public AdminService(
            PurchaseOrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            WishlistItemRepository wishlistItemRepository,
            ProductRepository productRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.wishlistItemRepository = wishlistItemRepository;
        this.productRepository = productRepository;
    }

    /**
     * Lista pedidos con filtros opcionales por estado del pedido y del correo
     * (por ejemplo, {@code emailStatus=FAILED} para ver los comprobantes no enviados).
     *
     * @param status estado del pedido (opcional)
     * @param emailStatus estado del correo (opcional)
     * @param pageable paginación y orden
     * @return página de pedidos
     */
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> listOrders(
            OrderStatus status, EmailStatus emailStatus, Pageable pageable) {

        Page<PurchaseOrder> page;

        if (status != null && emailStatus != null) {
            page = orderRepository.findByStatusAndEmailStatus(status, emailStatus, pageable);
        } else if (status != null) {
            page = orderRepository.findByStatus(status, pageable);
        } else if (emailStatus != null) {
            page = orderRepository.findByEmailStatus(emailStatus, pageable);
        } else {
            page = orderRepository.findAll(pageable);
        }

        return PageResponse.from(page.map(order -> OrderResponse.from(order, true)));
    }

    /**
     * Cambia el estado de un pedido respetando el flujo permitido:
     * PAID a SHIPPED o CANCELLED, y SHIPPED a DELIVERED. Al cancelar se devuelve el stock.
     *
     * @param orderId identificador del pedido
     * @param newStatus nuevo estado
     * @return pedido actualizado
     * @throws ResourceNotFoundException si el pedido no existe
     * @throws BusinessException si la transición no está permitida
     */
    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {

        // Bloqueado: dos cambios simultáneos no pueden devolver el stock dos veces.
        PurchaseOrder order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("El pedido no existe"));

        OrderStatus current = order.getStatus();

        if (!isAllowed(current, newStatus)) {
            throw new BusinessException("No se puede cambiar un pedido de " + current + " a " + newStatus);
        }

        if (newStatus == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                productRepository.increaseStock(item.getProductId(), item.getQuantity());
            }
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        return OrderResponse.from(order, true);
    }

    /**
     * Reporte de ventas de un periodo.
     *
     * @param from primer día (incluido)
     * @param to último día (incluido)
     * @return totales del periodo
     * @throws BusinessException si el rango de fechas es inválido
     */
    @Transactional(readOnly = true)
    public SalesReportResponse salesReport(LocalDate from, LocalDate to) {

        requireValidRange(from, to);

        List<PurchaseOrder> orders = orderRepository.findForSalesReport(
                OrderStatus.CANCELLED, from.atStartOfDay(), to.plusDays(1).atStartOfDay());

        BigDecimal totalSales = orders.stream()
                .map(PurchaseOrder::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageTicket = orders.isEmpty()
                ? BigDecimal.ZERO
                : totalSales.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);

        return new SalesReportResponse(from, to, orders.size(), totalSales, averageTicket);
    }

    /**
     * Productos más vendidos de un periodo.
     *
     * @param from primer día (incluido)
     * @param to último día (incluido)
     * @param limit cantidad máxima de productos (1 a 50)
     * @return productos ordenados por unidades vendidas
     * @throws BusinessException si el rango de fechas es inválido
     */
    @Transactional(readOnly = true)
    public List<TopProductResponse> topProducts(LocalDate from, LocalDate to, int limit) {

        requireValidRange(from, to);

        List<Object[]> rows = orderItemRepository.findTopProducts(
                OrderStatus.CANCELLED,
                from.atStartOfDay(),
                to.plusDays(1).atStartOfDay(),
                PageRequest.of(0, safeLimit(limit)));

        return rows.stream()
                .map(row -> new TopProductResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        new BigDecimal(String.valueOf(row[3]))))
                .toList();
    }

    /**
     * Productos más guardados en las wishlists.
     *
     * @param limit cantidad máxima de productos (1 a 50)
     * @return productos ordenados por veces guardados
     */
    @Transactional(readOnly = true)
    public List<WishlistReportResponse> wishlistReport(int limit) {

        List<Object[]> rows = wishlistItemRepository.findMostSaved(
                WishlistStatus.LIKE, PageRequest.of(0, safeLimit(limit)));

        return rows.stream()
                .map(row -> new WishlistReportResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        row[3] == null ? 0L : ((Number) row[3]).longValue()))
                .toList();
    }

    private boolean isAllowed(OrderStatus current, OrderStatus next) {
        return switch (current) {
            case PAID -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    private void requireValidRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessException("La fecha inicial no puede ser posterior a la fecha final");
        }
    }

    private int safeLimit(int limit) {
        return Math.min(Math.max(limit, 1), MAX_REPORT_ROWS);
    }
}
