package com.template.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.template.api.dto.request.CreateProductRequest;
import com.template.api.dto.request.PatchProductRequest;
import com.template.api.dto.request.UpdateProductRequest;
import com.template.api.dto.response.ProductResponse;
import com.template.api.entity.Product;
import com.template.api.entity.ProductStatus;
import com.template.api.exception.DuplicateResourceException;
import com.template.api.exception.ResourceNotFoundException;
import com.template.api.mapper.ProductMapper;
import com.template.api.repository.ProductRepository;
import com.template.api.service.impl.ProductServiceImpl;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository, new ProductMapper());
    }

    @Test
    void create_WhenSkuIsUnique_SavesAndReturnsProduct() {
        CreateProductRequest request = new CreateProductRequest(
                "sku-100", "Mechanical Keyboard", "RGB switches",
                new BigDecimal("125.00"), 15, ProductStatus.ACTIVE
        );

        when(productRepository.existsBySkuIgnoreCase("SKU-100")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });

        ProductResponse response = productService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.sku()).isEqualTo("SKU-100");
        assertThat(response.price()).isEqualByComparingTo("125.00");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void create_WhenSkuExists_ThrowsDuplicateResourceException() {
        CreateProductRequest request = new CreateProductRequest(
                "SKU-100", "Keyboard", null,
                new BigDecimal("50.00"), 5, ProductStatus.ACTIVE
        );

        when(productRepository.existsBySkuIgnoreCase("SKU-100")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("SKU-100");

        verify(productRepository, never()).save(any());
    }

    @Test
    void getById_WhenNotFound_ThrowsResourceNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_ReplacesMutableFields() {
        Product existing = new Product(
                "SKU-200", "Old Name", "Old Desc",
                new BigDecimal("10.00"), 5, ProductStatus.ACTIVE
        );
        ReflectionTestUtils.setField(existing, "id", 2L);

        when(productRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateProductRequest update = new UpdateProductRequest(
                "New Name", "New Desc", new BigDecimal("25.50"), 12, ProductStatus.INACTIVE
        );

        ProductResponse result = productService.update(2L, update);

        assertThat(result.name()).isEqualTo("New Name");
        assertThat(result.price()).isEqualByComparingTo("25.50");
        assertThat(result.stockQuantity()).isEqualTo(12);
        assertThat(result.status()).isEqualTo(ProductStatus.INACTIVE);
    }

    @Test
    void patch_UpdatesOnlyProvidedFields() {
        Product existing = new Product(
                "SKU-200", "Old Name", "Old Desc",
                new BigDecimal("10.00"), 5, ProductStatus.ACTIVE
        );
        ReflectionTestUtils.setField(existing, "id", 2L);

        when(productRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        PatchProductRequest patch = new PatchProductRequest(
                null, null, new BigDecimal("19.99"), null, ProductStatus.OUT_OF_STOCK
        );

        ProductResponse result = productService.patch(2L, patch);

        assertThat(result.name()).isEqualTo("Old Name");
        assertThat(result.description()).isEqualTo("Old Desc");
        assertThat(result.price()).isEqualByComparingTo("19.99");
        assertThat(result.stockQuantity()).isEqualTo(5);
        assertThat(result.status()).isEqualTo(ProductStatus.OUT_OF_STOCK);
    }

    @Test
    void delete_WhenExists_DeletesEntity() {
        Product existing = new Product(
                "SKU-300", "Mouse", null,
                new BigDecimal("15.00"), 3, ProductStatus.ACTIVE
        );
        when(productRepository.findById(3L)).thenReturn(Optional.of(existing));

        productService.delete(3L);

        verify(productRepository).delete(existing);
    }
}
