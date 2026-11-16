package co.edu.unbosque.tstyle.admin.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.common.PageResponse;
import co.edu.unbosque.tstyle.exception.BusinessException;
import co.edu.unbosque.tstyle.exception.ResourceNotFoundException;
import co.edu.unbosque.tstyle.order.dto.OrderDetailResponse;
import co.edu.unbosque.tstyle.order.dto.OrderSummaryResponse;
import co.edu.unbosque.tstyle.order.enums.EmailStatus;
import co.edu.unbosque.tstyle.order.enums.OrderStatus;
import co.edu.unbosque.tstyle.order.model.OrderItem;
import co.edu.unbosque.tstyle.order.model.PurchaseOrder;
import co.edu.unbosque.tstyle.order.repository.PurchaseOrderRepository;
import co.edu.unbosque.tstyle.product.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;

/**
 * Lógica de administración de pedidos: búsqueda con filtros y cambio de estado.
 *
 * @author Equipo T Style
 */
@Service
public class AdminOrderService {

    private final PurchaseOrderRepository orderRepository;
    private final ProductRepository productRepository;

    /**
     * Constructor del servicio de administración de pedidos.
     *
     * @param orderRepository repositorio de pedidos
     * @param productRepository repositorio de productos
     */
    public AdminOrderService(
            PurchaseOrderRepository orderRepository,
            ProductRepository productRepository) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    /**
     * Busca pedidos con filtros opcionales.
     *
     * @param status estado del pedido (opcional)
     * @param emailStatus estado del correo, por ejemplo FAILED (opcional)
     * @param userId comprador (opcional)
     * @param from fecha inicial incluida (opcional)
     * @param to fecha final excluida (opcional)
     * @param pageable paginación y orden
     * @return página de resúmenes de pedidos
     */
    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> search(
            OrderStatus status, EmailStatus emailStatus, Long userId,
            LocalDateTime from, LocalDateTime to, Pageable pageable) {

        Specification<PurchaseOrder> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (emailStatus != null) {
                predicates.add(cb.equal(root.get("emailStatus"), emailStatus));
            }
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThan(root.<LocalDateTime>get("createdAt"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<OrderSummaryResponse> page = orderRepository.findAll(spec, pageable)
                .map(OrderSummaryResponse::from);

        return PageResponse.from(page);
    }

    /**
     * Cambia el estado de un pedido respetando el flujo permitido:
     * PAID → SHIPPED o CANCELLED, y SHIPPED → DELIVERED.
     * Al cancelar se devuelve el stock de los productos.
     *
     * @param orderId identificador del pedido
     * @param newStatus nuevo estado
     * @return pedido actualizado
     * @throws ResourceNotFoundException si el pedido no existe
     * @throws BusinessException si la transición no está permitida
     */
    @Transactional
    public OrderDetailResponse updateStatus(Long orderId, OrderStatus newStatus) {

        // Bloqueado: dos cambios simultáneos no pueden devolver el stock dos veces.
        PurchaseOrder order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("El pedido no existe"));

        OrderStatus current = order.getStatus();

        if (!isAllowed(current, newStatus)) {
            throw new BusinessException(
                    "No se puede cambiar un pedido de " + current + " a " + newStatus);
        }

        if (newStatus == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                productRepository.increaseStock(item.getProductId(), item.getQuantity());
            }
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        return OrderDetailResponse.from(order, true);
    }

    private boolean isAllowed(OrderStatus current, OrderStatus next) {
        return switch (current) {
            case PAID -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
