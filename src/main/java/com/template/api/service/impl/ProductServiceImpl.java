package com.template.api.service.impl;

import com.template.api.dto.request.CreateProductRequest;
import com.template.api.dto.request.PatchProductRequest;
import com.template.api.dto.request.UpdateProductRequest;
import com.template.api.dto.response.PagedResponse;
import com.template.api.dto.response.ProductResponse;
import com.template.api.entity.Product;
import com.template.api.entity.ProductStatus;
import com.template.api.exception.DuplicateResourceException;
import com.template.api.exception.ResourceNotFoundException;
import com.template.api.mapper.ProductMapper;
import com.template.api.repository.ProductRepository;
import com.template.api.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        String normalizedSku = request.sku().trim().toUpperCase();
        if (productRepository.existsBySkuIgnoreCase(normalizedSku)) {
            throw new DuplicateResourceException("Product", "sku", normalizedSku);
        }

        Product saved = productRepository.save(productMapper.toEntity(request));
        return productMapper.toResponse(saved);
    }

    @Override
    public ProductResponse getById(Long id) {
        return productMapper.toResponse(findProductOrThrow(id));
    }

    @Override
    public PagedResponse<ProductResponse> getAll(String search, ProductStatus status, Pageable pageable) {
        boolean hasSearch = search != null && !search.isBlank();
        Page<Product> page;

        if (status != null && hasSearch) {
            page = productRepository.findByStatusAndNameContainingIgnoreCase(status, search.trim(), pageable);
        } else if (status != null) {
            page = productRepository.findByStatus(status, pageable);
        } else if (hasSearch) {
            page = productRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            page = productRepository.findAll(pageable);
        }

        return PagedResponse.from(page.map(productMapper::toResponse));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = findProductOrThrow(id);
        product.update(
                request.name().trim(),
                request.description(),
                request.price(),
                request.stockQuantity(),
                request.status()
        );

        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse patch(Long id, PatchProductRequest request) {
        Product product = findProductOrThrow(id);
        product.patch(
                request.name(),
                request.description(),
                request.price(),
                request.stockQuantity(),
                request.status()
        );

        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = findProductOrThrow(id);
        productRepository.delete(product);
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }
}
