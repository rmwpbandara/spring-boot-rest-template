package com.template.api.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.template.api.dto.request.CreateProductRequest;
import com.template.api.dto.request.PatchProductRequest;
import com.template.api.dto.response.ProductResponse;
import com.template.api.entity.ProductStatus;
import com.template.api.exception.GlobalExceptionHandler;
import com.template.api.exception.ResourceNotFoundException;
import com.template.api.service.ProductService;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    void createProduct_ValidRequest_Returns201WithLocationHeader() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "SKU-001", "Laptop", "Workstation",
                new BigDecimal("1499.99"), 10, ProductStatus.ACTIVE
        );
        ProductResponse response = new ProductResponse(
                1L, "SKU-001", "Laptop", "Workstation",
                new BigDecimal("1499.99"), 10, ProductStatus.ACTIVE,
                Instant.now(), Instant.now()
        );

        when(productService.create(any(CreateProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/products/1")))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.sku").value("SKU-001"));
    }

    @Test
    void createProduct_InvalidRequest_Returns400WithFieldViolations() throws Exception {
        CreateProductRequest invalid = new CreateProductRequest(
                "", "", null, new BigDecimal("-5.00"), -1, null
        );

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors", hasSize(4)));
    }

    @Test
    void getById_WhenNotFound_Returns404() throws Exception {
        when(productService.getById(99L))
                .thenThrow(new ResourceNotFoundException("Product", "id", 99L));

        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.fieldErrors", hasSize(0)));
    }

    @Test
    void getAll_WithInvalidSortField_Returns400() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("sortBy", "nonExistentField"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void patchProduct_ValidRequest_Returns200() throws Exception {
        PatchProductRequest patch = new PatchProductRequest(
                null, null, new BigDecimal("1299.99"), null, null
        );
        ProductResponse response = new ProductResponse(
                1L, "SKU-001", "Laptop", "Workstation",
                new BigDecimal("1299.99"), 10, ProductStatus.ACTIVE,
                Instant.now(), Instant.now()
        );

        when(productService.patch(eq(1L), any(PatchProductRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.price").value(1299.99));
    }

    @Test
    void deleteProduct_Returns204() throws Exception {
        doNothing().when(productService).delete(1L);

        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());
    }
}
