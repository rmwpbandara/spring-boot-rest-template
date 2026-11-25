package com.template.api.service;

import com.template.api.dto.request.CreateProductRequest;
import com.template.api.dto.request.PatchProductRequest;
import com.template.api.dto.request.UpdateProductRequest;
import com.template.api.dto.response.PagedResponse;
import com.template.api.dto.response.ProductResponse;
import com.template.api.entity.ProductStatus;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponse create(CreateProductRequest request);

    ProductResponse getById(Long id);

    PagedResponse<ProductResponse> getAll(String search, ProductStatus status, Pageable pageable);

    ProductResponse update(Long id, UpdateProductRequest request);

    ProductResponse patch(Long id, PatchProductRequest request);

    void delete(Long id);
}
