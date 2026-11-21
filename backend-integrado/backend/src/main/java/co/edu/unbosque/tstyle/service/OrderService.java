package co.edu.unbosque.tstyle.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.dto.OrderResponse;
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.exception.BusinessException;
import co.edu.unbosque.tstyle.exception.ResourceNotFoundException;
import co.edu.unbosque.tstyle.model.CartItem;
import co.edu.unbosque.tstyle.model.OrderItem;
import co.edu.unbosque.tstyle.model.Product;
import co.edu.unbosque.tstyle.model.PurchaseOrder;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.repository.CartItemRepository;
import co.edu.unbosque.tstyle.repository.ProductRepository;
import co.edu.unbosque.tstyle.repository.PurchaseOrderRepository;

@Service
public class OrderService {
    private final PurchaseOrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public OrderService(
            PurchaseOrderRepository orderRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse checkout(User user) {
        List<CartItem> cartItems = new ArrayList<>(cartItemRepository.findForCheckout(user.getId()));

        if (cartItems.isEmpty()) {
            throw new BusinessException("El carrito está vacío");
        }

        cartItems.sort(Comparator.comparing(item -> item.getProduct().getId()));

        PurchaseOrder order = new PurchaseOrder(user);
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            if (!product.isActive()) {
                throw new BusinessException("El producto \"" + product.getName() + "\" ya no está disponible");
            }

            if (productRepository.decreaseStock(product.getId(), cartItem.getQuantity()) == 0) {
                throw new BusinessException("Stock insuficiente para el producto \"" + product.getName() + "\"");
            }

            OrderItem orderItem = new OrderItem(
                    order, product.getId(), product.getName(), product.getPrice(), cartItem.getQuantity());

            order.addItem(orderItem);
            total = total.add(orderItem.getSubtotal());
        }

        order.setTotal(total);

        orderRepository.save(order);
        cartItemRepository.deleteAll(cartItems);

        return OrderResponse.from(order, false);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listMine(User user) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(order -> OrderResponse.from(order, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(User requester, Long orderId) {
        PurchaseOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("El pedido no existe"));

        boolean admin = requester.getRole() == Role.ADMIN;

        if (!admin && !order.getUser().getId().equals(requester.getId())) {
            throw new ResourceNotFoundException("El pedido no existe");
        }

        return OrderResponse.from(order, admin);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderForReceipt(Long orderId) {
        PurchaseOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("El pedido no existe"));

        return OrderResponse.from(order, false);
    }
}
