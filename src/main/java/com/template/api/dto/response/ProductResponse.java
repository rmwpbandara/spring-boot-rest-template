package com.template.api.dto.response;

import com.template.api.entity.ProductStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
