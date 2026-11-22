package co.edu.unbosque.tstyle.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.dto.CartLineResponse;
import co.edu.unbosque.tstyle.dto.CartResponse;
import co.edu.unbosque.tstyle.exception.BusinessException;
import co.edu.unbosque.tstyle.exception.ResourceNotFoundException;
import co.edu.unbosque.tstyle.model.CartItem;
import co.edu.unbosque.tstyle.model.Product;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.repository.CartItemRepository;
import co.edu.unbosque.tstyle.repository.ProductRepository;

@Service
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(User user) {
        return buildResponse(user);
    }

    @Transactional
    public CartResponse addItem(User user, Long productId, int quantity) {
        requirePositive(quantity);

        Product product = productRepository.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El producto no existe o no está disponible"));

        CartItem item = cartItemRepository
                .findByUserIdAndProductId(user.getId(), productId)
                .orElse(null);

        int newQuantity = quantity + (item == null ? 0 : item.getQuantity());
        requireStock(product, newQuantity);

        if (item == null) {
            item = new CartItem(user, product, newQuantity);
        } else {
            item.setQuantity(newQuantity);
        }

        cartItemRepository.save(item);

        return buildResponse(user);
    }

    @Transactional
    public CartResponse updateItem(User user, Long productId, int quantity) {
        requirePositive(quantity);

        CartItem item = findCartItem(user, productId);
        Product product = item.getProduct();

        if (!product.isActive()) {
            throw new BusinessException("El producto \"" + product.getName() + "\" ya no está disponible");
        }
        requireStock(product, quantity);

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return buildResponse(user);
    }

    @Transactional
    public CartResponse removeItem(User user, Long productId) {
        cartItemRepository.delete(findCartItem(user, productId));

        return buildResponse(user);
    }

    @Transactional
    public CartResponse clear(User user) {
        cartItemRepository.deleteByUserId(user.getId());

        return new CartResponse(List.of(), 0, BigDecimal.ZERO);
    }

    private CartItem findCartItem(User user, Long productId) {
        return cartItemRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("El producto no está en el carrito"));
    }

    private void requirePositive(int quantity) {
        if (quantity < 1) {
            throw new BusinessException("La cantidad debe ser al menos 1");
        }
    }

    private void requireStock(Product product, int quantity) {
        if (product.getStock() < quantity) {
            throw new BusinessException("Solo hay " + product.getStock()
                    + " unidades disponibles de \"" + product.getName() + "\"");
        }
    }

    private CartResponse buildResponse(User user) {
        List<CartLineResponse> lines = cartItemRepository.findByUserIdOrderById(user.getId())
                .stream()
                .map(this::toLine)
                .toList();

        int totalItems = lines.stream().mapToInt(CartLineResponse::quantity).sum();

        BigDecimal total = lines.stream()
                .map(CartLineResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(lines, totalItems, total);
    }

    private CartLineResponse toLine(CartItem item) {
        Product product = item.getProduct();

        BigDecimal subtotal = product.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);

        return new CartLineResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                item.getQuantity(),
                subtotal,
                product.getStock(),
                product.isActive() && product.getStock() >= item.getQuantity());
    }
}
