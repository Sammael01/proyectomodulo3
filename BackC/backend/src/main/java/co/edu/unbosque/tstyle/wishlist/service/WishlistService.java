package co.edu.unbosque.tstyle.wishlist.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.cart.dto.CartResponse;
import co.edu.unbosque.tstyle.cart.service.CartService;
import co.edu.unbosque.tstyle.exception.ResourceNotFoundException;
import co.edu.unbosque.tstyle.product.model.Product;
import co.edu.unbosque.tstyle.product.repository.ProductRepository;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.wishlist.dto.WishlistItemResponse;
import co.edu.unbosque.tstyle.wishlist.enums.WishlistStatus;
import co.edu.unbosque.tstyle.wishlist.model.WishlistItem;
import co.edu.unbosque.tstyle.wishlist.repository.WishlistItemRepository;

/**
 * Contiene la lógica de negocio de la wishlist personal.
 *
 * @author Equipo T Style
 */
@Service
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    /**
     * Constructor del servicio de la wishlist.
     *
     * @param wishlistItemRepository repositorio de la wishlist
     * @param productRepository repositorio de productos
     * @param cartService servicio del carrito
     */
    public WishlistService(
            WishlistItemRepository wishlistItemRepository,
            ProductRepository productRepository,
            CartService cartService) {

        this.wishlistItemRepository = wishlistItemRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    /**
     * Lista la wishlist del usuario, opcionalmente filtrada por estado.
     *
     * @param user usuario autenticado
     * @param status estado a filtrar, o null para todos
     * @return productos de la wishlist
     */
    @Transactional(readOnly = true)
    public List<WishlistItemResponse> list(User user, WishlistStatus status) {

        List<WishlistItem> items = (status == null)
                ? wishlistItemRepository.findAllByUser(user.getId())
                : wishlistItemRepository.findAllByUserAndStatus(user.getId(), status);

        return items.stream().map(WishlistItemResponse::from).toList();
    }

    /**
     * Guarda un producto en la wishlist o, si ya estaba, actualiza su estado.
     * Nunca crea duplicados.
     *
     * @param user usuario autenticado
     * @param productId producto a guardar
     * @param status estado deseado (null equivale a WATCH_LATER)
     * @return elemento guardado
     * @throws ResourceNotFoundException si el producto no existe
     */
    @Transactional
    public WishlistItemResponse save(User user, Long productId, WishlistStatus status) {

        WishlistStatus finalStatus = (status == null) ? WishlistStatus.WATCH_LATER : status;

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("El producto no existe"));

        WishlistItem item = wishlistItemRepository
                .findByUserIdAndProductId(user.getId(), productId)
                .orElse(null);

        if (item == null) {
            item = new WishlistItem(user, product, finalStatus);
        } else {
            item.setStatus(finalStatus);
        }

        return WishlistItemResponse.from(wishlistItemRepository.saveAndFlush(item));
    }

    /**
     * Quita un producto de la wishlist.
     *
     * @param user usuario autenticado
     * @param productId producto a quitar
     * @throws ResourceNotFoundException si el producto no estaba en la wishlist
     */
    @Transactional
    public void remove(User user, Long productId) {

        WishlistItem item = findItem(user, productId);
        wishlistItemRepository.delete(item);
    }

    /**
     * Mueve un producto de la wishlist al carrito. Todo ocurre en una sola
     * transacción: si el carrito rechaza el producto (sin stock, por ejemplo),
     * el producto sigue en la wishlist.
     *
     * @param user usuario autenticado
     * @param productId producto a mover
     * @param quantity unidades a agregar al carrito
     * @return resumen actualizado del carrito
     */
    @Transactional
    public CartResponse moveToCart(User user, Long productId, int quantity) {

        WishlistItem item = findItem(user, productId);

        CartResponse cart = cartService.addItem(user, productId, quantity);

        wishlistItemRepository.delete(item);

        return cart;
    }

    private WishlistItem findItem(User user, Long productId) {
        return wishlistItemRepository
                .findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El producto no está en tu wishlist"));
    }
}
