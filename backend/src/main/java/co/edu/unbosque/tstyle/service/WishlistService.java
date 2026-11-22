package co.edu.unbosque.tstyle.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.dto.CartResponse;
import co.edu.unbosque.tstyle.dto.WishlistItemResponse;
import co.edu.unbosque.tstyle.enums.WishlistStatus;
import co.edu.unbosque.tstyle.exception.ResourceNotFoundException;
import co.edu.unbosque.tstyle.model.Product;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.model.WishlistItem;
import co.edu.unbosque.tstyle.repository.ProductRepository;
import co.edu.unbosque.tstyle.repository.WishlistItemRepository;

@Service
public class WishlistService {
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    public WishlistService(
            WishlistItemRepository wishlistItemRepository,
            ProductRepository productRepository,
            CartService cartService) {
        this.wishlistItemRepository = wishlistItemRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    @Transactional(readOnly = true)
    public List<WishlistItemResponse> list(User user, WishlistStatus status) {
        List<WishlistItem> items = (status == null)
                ? wishlistItemRepository.findByUserIdOrderByUpdatedAtDesc(user.getId())
                : wishlistItemRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(user.getId(), status);

        return items.stream().map(WishlistItemResponse::from).toList();
    }

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

    @Transactional
    public void remove(User user, Long productId) {
        wishlistItemRepository.delete(findItem(user, productId));
    }

    @Transactional
    public CartResponse moveToCart(User user, Long productId, int quantity) {
        WishlistItem item = findItem(user, productId);

        CartResponse cart = cartService.addItem(user, productId, quantity);

        wishlistItemRepository.delete(item);

        return cart;
    }

    private WishlistItem findItem(User user, Long productId) {
        return wishlistItemRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("El producto no está en tu wishlist"));
    }
}
