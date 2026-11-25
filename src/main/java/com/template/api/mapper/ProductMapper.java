package com.template.api.mapper;

import com.template.api.dto.request.CreateProductRequest;
import com.template.api.dto.response.ProductResponse;
import com.template.api.entity.Product;
import com.template.api.entity.ProductStatus;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(CreateProductRequest request) {
        return new Product(
                request.sku().trim().toUpperCase(),
                request.name().trim(),
                request.description(),
                request.price(),
                request.stockQuantity(),
                request.status() != null ? request.status() : ProductStatus.ACTIVE
        );
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
