package co.edu.unbosque.tstyle.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.tstyle.dto.PageResponse;
import co.edu.unbosque.tstyle.dto.ProductResponse;
import co.edu.unbosque.tstyle.exception.ResourceNotFoundException;
import co.edu.unbosque.tstyle.mapper.ProductMapper;
import co.edu.unbosque.tstyle.model.Product;
import co.edu.unbosque.tstyle.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(Long categoryId, Long providerId,
            String search, Pageable pageable) {
        String term = (search == null || search.isBlank()) ? null : search.trim();

        Page<Product> page = productRepository.search(categoryId, providerId, term, pageable);

        List<ProductResponse> content = page.getContent().stream()
                .map(productMapper::toResponse)
                .toList();

        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .filter(found -> found.isActive() && found.getProvider().isActive())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el producto con id " + id));

        return productMapper.toResponse(product);
    }
}
