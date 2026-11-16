package co.edu.unbosque.tstyle.cart.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.cart.dto.CartLimits;
import co.edu.unbosque.tstyle.cart.dto.CartLineResponse;
import co.edu.unbosque.tstyle.cart.dto.CartResponse;
import co.edu.unbosque.tstyle.cart.model.CartItem;
import co.edu.unbosque.tstyle.cart.repository.CartItemRepository;
import co.edu.unbosque.tstyle.exception.BusinessException;
import co.edu.unbosque.tstyle.exception.ResourceNotFoundException;
import co.edu.unbosque.tstyle.product.model.Product;
import co.edu.unbosque.tstyle.product.repository.ProductRepository;
import co.edu.unbosque.tstyle.user.model.User;

/**
 * Contiene la lógica de negocio del carrito de compras.
 *
 * <p>Los precios y totales siempre se calculan aquí con los datos de la
 * base de datos; nunca se confía en valores enviados por el cliente.</p>
 *
 * @author Equipo T Style
 */
@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    /**
     * Constructor del servicio del carrito.
     *
     * @param cartItemRepository repositorio del carrito
     * @param productRepository repositorio de productos
     */
    public CartService(
            CartItemRepository cartItemRepository,
            ProductRepository productRepository) {

        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    /**
     * Obtiene el resumen del carrito del usuario.
     *
     * @param user usuario autenticado
     * @return resumen del carrito
     */
    @Transactional(readOnly = true)
    public CartResponse getCart(User user) {
        return buildResponse(cartItemRepository.findAllByUserWithProduct(user.getId()));
    }

    /**
     * Agrega unidades de un producto al carrito. Si el producto ya estaba,
     * suma la cantidad en la misma línea (no crea duplicados).
     *
     * @param user usuario autenticado
     * @param productId producto a agregar
     * @param quantity unidades a agregar
     * @return resumen actualizado del carrito
     * @throws BusinessException si la cantidad o el stock no son válidos
     * @throws ResourceNotFoundException si el producto no existe o no está activo
     */
    @Transactional
    public CartResponse addItem(User user, Long productId, int quantity) {

        validateQuantity(quantity);

        Product product = findPurchasableProduct(productId);

        CartItem item = cartItemRepository
                .findByUserIdAndProductId(user.getId(), productId)
                .orElse(null);

        int newQuantity = (item == null ? 0 : item.getQuantity()) + quantity;

        validateQuantity(newQuantity);
        validateStock(product, newQuantity);

        if (item == null) {
            item = new CartItem(user, product, newQuantity);
        } else {
            item.setQuantity(newQuantity);
        }

        cartItemRepository.save(item);

        return buildResponse(cartItemRepository.findAllByUserWithProduct(user.getId()));
    }

    /**
     * Cambia la cantidad de un producto que ya está en el carrito.
     *
     * @param user usuario autenticado
     * @param productId producto a modificar
     * @param quantity nueva cantidad
     * @return resumen actualizado del carrito
     */
    @Transactional
    public CartResponse updateItem(User user, Long productId, int quantity) {

        validateQuantity(quantity);

        CartItem item = findCartItem(user, productId);

        Product product = item.getProduct();
        if (!product.isActive()) {
            throw new BusinessException("El producto \"" + product.getName() + "\" ya no está disponible");
        }
        validateStock(product, quantity);

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return buildResponse(cartItemRepository.findAllByUserWithProduct(user.getId()));
    }

    /**
     * Quita un producto del carrito.
     *
     * @param user usuario autenticado
     * @param productId producto a quitar
     * @return resumen actualizado del carrito
     */
    @Transactional
    public CartResponse removeItem(User user, Long productId) {

        CartItem item = findCartItem(user, productId);
        cartItemRepository.delete(item);

        return buildResponse(cartItemRepository.findAllByUserWithProduct(user.getId()));
    }

    /**
     * Vacía el carrito del usuario.
     *
     * @param user usuario autenticado
     * @return resumen del carrito vacío
     */
    @Transactional
    public CartResponse clear(User user) {

        cartItemRepository.deleteAllByUserId(user.getId());

        return buildResponse(List.of());
    }

    private CartItem findCartItem(User user, Long productId) {
        return cartItemRepository
                .findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("El producto no está en el carrito"));
    }

    private Product findPurchasableProduct(Long productId) {
        return productRepository.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El producto no existe o no está disponible"));
    }

    private void validateQuantity(int quantity) {

        if (quantity < 1) {
            throw new BusinessException("La cantidad debe ser al menos 1");
        }

        if (quantity > CartLimits.MAX_QUANTITY_PER_ITEM) {
            throw new BusinessException("No puedes tener más de "
                    + CartLimits.MAX_QUANTITY_PER_ITEM
                    + " unidades de un mismo producto en el carrito");
        }
    }

    private void validateStock(Product product, int quantity) {

        if (product.getStock() < quantity) {
            throw new BusinessException("Solo hay " + product.getStock()
                    + " unidades disponibles de \"" + product.getName() + "\"");
        }
    }

    private CartResponse buildResponse(List<CartItem> items) {

        List<CartLineResponse> lines = items.stream().map(this::toLine).toList();

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

        boolean available = product.isActive() && product.getStock() >= item.getQuantity();

        return new CartLineResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                item.getQuantity(),
                subtotal,
                product.getStock(),
                available);
    }
}
