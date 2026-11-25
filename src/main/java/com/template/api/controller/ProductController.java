package com.template.api.controller;

import com.template.api.dto.request.CreateProductRequest;
import com.template.api.dto.request.PatchProductRequest;
import com.template.api.dto.request.UpdateProductRequest;
import com.template.api.dto.response.ApiResponse;
import com.template.api.dto.response.PagedResponse;
import com.template.api.dto.response.ProductResponse;
import com.template.api.entity.ProductStatus;
import com.template.api.exception.BadRequestException;
import com.template.api.service.ProductService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "sku", "name", "price", "stockQuantity", "status", "createdAt", "updatedAt"
    );

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // POST /api/v1/products
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody CreateProductRequest request) {
        ProductResponse created = productService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.ok("Product created successfully", created));
    }

    // GET /api/v1/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Product retrieved successfully", productService.getById(id)));
    }

    // GET /api/v1/products?search=&status=&page=0&size=10&sortBy=id&sortDir=desc
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        if (page < 0) {
            throw new BadRequestException("Page index must not be negative");
        }
        if (size < 1) {
            throw new BadRequestException("Page size must be at least 1");
        }
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException("Invalid sortBy field: " + sortBy);
        }

        Sort sort = "asc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE), sort);

        PagedResponse<ProductResponse> result = productService.getAll(search, status, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Products retrieved successfully", result));
    }

    // PUT /api/v1/products/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductResponse updated = productService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Product updated successfully", updated));
    }

    // PATCH /api/v1/products/{id}
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> patch(
            @PathVariable Long id,
            @Valid @RequestBody PatchProductRequest request) {
        ProductResponse updated = productService.patch(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Product patched successfully", updated));
    }

    // DELETE /api/v1/products/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
