package co.edu.unbosque.tstyle.order.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.cart.model.CartItem;
import co.edu.unbosque.tstyle.cart.repository.CartItemRepository;
import co.edu.unbosque.tstyle.exception.BusinessException;
import co.edu.unbosque.tstyle.exception.ResourceNotFoundException;
import co.edu.unbosque.tstyle.order.dto.OrderDetailResponse;
import co.edu.unbosque.tstyle.order.dto.OrderSummaryResponse;
import co.edu.unbosque.tstyle.order.model.OrderItem;
import co.edu.unbosque.tstyle.order.model.PurchaseOrder;
import co.edu.unbosque.tstyle.order.repository.PurchaseOrderRepository;
import co.edu.unbosque.tstyle.product.model.Product;
import co.edu.unbosque.tstyle.product.repository.ProductRepository;
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;

/**
 * Contiene la lógica de negocio del checkout y de la consulta de pedidos.
 *
 * <p>El checkout es una sola transacción de base de datos. El PDF y el correo
 * NO forman parte de ella (ver {@link ReceiptMailService}), así un fallo de
 * correo nunca revierte ni duplica el pedido.</p>
 *
 * @author Equipo T Style
 */
@Service
public class OrderService {

    private final PurchaseOrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    /**
     * Constructor del servicio de pedidos.
     *
     * @param orderRepository repositorio de pedidos
     * @param cartItemRepository repositorio del carrito
     * @param productRepository repositorio de productos
     */
    public OrderService(
            PurchaseOrderRepository orderRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository) {

        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    /**
     * Convierte el carrito del usuario en un pedido (checkout simulado).
     *
     * <p>Dentro de una única transacción: bloquea el carrito, revalida
     * disponibilidad, descuenta el stock, guarda el pedido con el precio
     * vigente de cada producto y vacía el carrito. Si cualquier paso falla,
     * no queda nada a medias.</p>
     *
     * @param user comprador autenticado
     * @return pedido creado
     * @throws BusinessException si el carrito está vacío o algún producto no está disponible
     */
    @Transactional
    public OrderDetailResponse checkout(User user) {

        // Bloqueo: si llegan dos checkouts a la vez, el segundo espera y encuentra el carrito vacío.
        List<CartItem> cartItems = new ArrayList<>(
                cartItemRepository.findAllByUserForUpdate(user.getId()));

        if (cartItems.isEmpty()) {
            throw new BusinessException("El carrito está vacío");
        }

        // Orden fijo por producto para evitar bloqueos cruzados entre compras simultáneas.
        cartItems.sort(Comparator.comparing(item -> item.getProduct().getId()));

        PurchaseOrder order = new PurchaseOrder(user);
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            if (!product.isActive()) {
                throw new BusinessException(
                        "El producto \"" + product.getName() + "\" ya no está disponible");
            }

            if (productRepository.decreaseStock(product.getId(), cartItem.getQuantity()) == 0) {
                throw new BusinessException(
                        "Stock insuficiente para el producto \"" + product.getName() + "\"");
            }

            // Se guarda el precio vigente AHORA, no el que el cliente haya visto antes.
            OrderItem orderItem = new OrderItem(
                    order,
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    cartItem.getQuantity());

            order.addItem(orderItem);
            total = total.add(orderItem.getSubtotal());
        }

        order.setTotal(total);

        orderRepository.save(order);
        cartItemRepository.deleteAll(cartItems);

        return OrderDetailResponse.from(order, false);
    }

    /**
     * Lista los pedidos del usuario, del más reciente al más antiguo.
     *
     * @param user usuario autenticado
     * @return resúmenes de sus pedidos
     */
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> listMine(User user) {
        return orderRepository.findAllByUser(user.getId())
                .stream()
                .map(OrderSummaryResponse::from)
                .toList();
    }

    /**
     * Obtiene el detalle de un pedido. Un cliente solo puede ver los suyos;
     * un administrador puede ver cualquiera.
     *
     * @param requester usuario que consulta
     * @param orderId identificador del pedido
     * @return detalle del pedido
     * @throws ResourceNotFoundException si no existe o no pertenece al usuario
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(User requester, Long orderId) {

        PurchaseOrder order = orderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("El pedido no existe"));

        boolean admin = requester.getRole() == Role.ADMIN;

        // Se responde 404 (y no 403) para no revelar qué pedidos existen.
        if (!admin && !order.getUser().getId().equals(requester.getId())) {
            throw new ResourceNotFoundException("El pedido no existe");
        }

        return OrderDetailResponse.from(order, admin);
    }

    /**
     * Obtiene el detalle de un pedido sin validar propietario.
     * Uso interno del envío de comprobantes.
     *
     * @param orderId identificador del pedido
     * @return detalle del pedido
     * @throws ResourceNotFoundException si el pedido no existe
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderForReceipt(Long orderId) {

        PurchaseOrder order = orderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("El pedido no existe"));

        return OrderDetailResponse.from(order, false);
    }
}
