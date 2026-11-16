package co.edu.unbosque.tstyle.admin.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.admin.dto.DailySalesResponse;
import co.edu.unbosque.tstyle.admin.dto.SalesReportResponse;
import co.edu.unbosque.tstyle.admin.dto.TopProductResponse;
import co.edu.unbosque.tstyle.admin.dto.WishlistReportResponse;
import co.edu.unbosque.tstyle.exception.BusinessException;
import co.edu.unbosque.tstyle.order.enums.OrderStatus;
import co.edu.unbosque.tstyle.order.model.PurchaseOrder;
import co.edu.unbosque.tstyle.order.repository.OrderItemRepository;
import co.edu.unbosque.tstyle.order.repository.PurchaseOrderRepository;
import co.edu.unbosque.tstyle.wishlist.enums.WishlistStatus;
import co.edu.unbosque.tstyle.wishlist.repository.WishlistItemRepository;

/**
 * Reportes de administración: ventas, productos más vendidos y
 * productos más guardados en wishlists.
 *
 * <p>Los pedidos cancelados no cuentan como ventas. Los reportes concretos
 * deben confirmarse con el equipo; estos son un punto de partida.</p>
 *
 * @author Equipo T Style
 */
@Service
public class ReportService {

    private static final int MAX_LIMIT = 50;

    private final PurchaseOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WishlistItemRepository wishlistItemRepository;

    /**
     * Constructor del servicio de reportes.
     *
     * @param orderRepository repositorio de pedidos
     * @param orderItemRepository repositorio del detalle de pedidos
     * @param wishlistItemRepository repositorio de la wishlist
     */
    public ReportService(
            PurchaseOrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            WishlistItemRepository wishlistItemRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.wishlistItemRepository = wishlistItemRepository;
    }

    /**
     * Reporte de ventas de un periodo, con desglose diario.
     *
     * @param from primer día (incluido)
     * @param to último día (incluido)
     * @return reporte de ventas
     * @throws BusinessException si el rango de fechas es inválido
     */
    @Transactional(readOnly = true)
    public SalesReportResponse sales(LocalDate from, LocalDate to) {

        validateRange(from, to);

        List<PurchaseOrder> orders = orderRepository.findForSalesReport(
                OrderStatus.CANCELLED, from.atStartOfDay(), to.plusDays(1).atStartOfDay());

        Map<LocalDate, List<PurchaseOrder>> byDay = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreatedAt().toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()));

        List<DailySalesResponse> daily = byDay.entrySet().stream()
                .map(entry -> new DailySalesResponse(
                        entry.getKey(), entry.getValue().size(), sumTotals(entry.getValue())))
                .toList();

        BigDecimal totalSales = sumTotals(orders);

        BigDecimal averageTicket = orders.isEmpty()
                ? BigDecimal.ZERO
                : totalSales.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);

        return new SalesReportResponse(from, to, orders.size(), totalSales, averageTicket, daily);
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

        validateRange(from, to);

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        List<Object[]> rows = orderItemRepository.findTopProducts(
                OrderStatus.CANCELLED, start, end, PageRequest.of(0, safeLimit(limit)));

        return rows.stream()
                .map(row -> new TopProductResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        toBigDecimal(row[3])))
                .toList();
    }

    /**
     * Productos más guardados en las wishlists de los usuarios.
     *
     * @param limit cantidad máxima de productos (1 a 50)
     * @return productos ordenados por veces guardados
     */
    @Transactional(readOnly = true)
    public List<WishlistReportResponse> mostSavedInWishlists(int limit) {

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

    private void validateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessException("La fecha inicial no puede ser posterior a la fecha final");
        }
    }

    private int safeLimit(int limit) {
        return Math.min(Math.max(limit, 1), MAX_LIMIT);
    }

    private BigDecimal sumTotals(List<PurchaseOrder> orders) {
        return orders.stream()
                .map(PurchaseOrder::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(value.toString());
    }
}
