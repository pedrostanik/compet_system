package com.petshop.api.products.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petshop.api.auth.filter.JwtFilter;
import com.petshop.api.products.domain.enums.AnimalTarget;
import com.petshop.api.products.domain.enums.ProductCategory;
import com.petshop.api.products.domain.enums.ProductStatus;
import com.petshop.api.products.dto.ProductRequest;
import com.petshop.api.products.dto.ProductResponse;
import com.petshop.api.products.dto.SupplierSummary;
import com.petshop.api.products.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private final Long PRODUCT_ID = 0L; 

    private ProductResponse buildResponse() {
        return new ProductResponse(
                PRODUCT_ID,
                "RAC-CAE-001",
                "Ração Premium Adulto 15kg",
                "7891234560001",
                ProductCategory.ALI,
                AnimalTarget.DOG,
                "Golden",
                "KG",
                new BigDecimal("85.00"),
                new BigDecimal("139.90"),
                new BigDecimal("0.3924"),
                new BigDecimal("10.000"),
                new BigDecimal("5.000"),
                false,
                "A3-P2",
                "2309.90.00",
                false,
                ProductStatus.ACTIVE,
                new SupplierSummary(1L, "FOR-001", "Distribuidora PetBrasil"),
                LocalDateTime.of(2026, 1, 10, 9, 0)
        );
    }

    private ProductRequest buildRequest() {
        return new ProductRequest(
                "Ração Premium Adulto 15kg",   // name
                ProductCategory.ALI,           // category
                AnimalTarget.DOG,               // animalTarget
                "Golden",                       // brand
                "KG",                           // unit
                new BigDecimal("85.00"),        // costPrice
                new BigDecimal("139.90"),       // salePrice
                "7891234560001",                // barcode
                new BigDecimal("5.000"),        // minStockQty
                new BigDecimal("10.000"),       // currentStockQty
                "A3-P2",                        // shelfLocation
                "2309.90.00",                   // ncm
                false,                          // loose
                null,                           // size
                null,                           // color
                1L                              // supplierId
        );
    }

    @Test
    void shouldCreateProduct() throws Exception {
        when(productService.create(any())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest()))
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("RAC-CAE-001"))
                .andExpect(jsonPath("$.name").value("Ração Premium Adulto 15kg"))
                .andExpect(jsonPath("$.category").value("ALI"));
    }

    @Test
    void shouldListAllProducts() throws Exception {
        when(productService.findAll()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("RAC-CAE-001"))
                .andExpect(jsonPath("$[0].name").value("Ração Premium Adulto 15kg"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void shouldGetProductById() throws Exception {
        when(productService.findById(PRODUCT_ID)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/products/{id}", PRODUCT_ID)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("RAC-CAE-001"))
                .andExpect(jsonPath("$.supplier.code").value("FOR-001"));
    }

    @Test
    void shouldReturn404WhenProductNotFound() throws Exception {
        Random random = new Random();
        long unknown = random.nextLong();
        when(productService.findById(unknown))
                .thenThrow(new EntityNotFoundException("Produto não encontrado: " + unknown));

        mockMvc.perform(get("/api/products/{id}", unknown)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetProductBySku() throws Exception {
        when(productService.findBySku("RAC-CAE-001")).thenReturn(buildResponse());

        mockMvc.perform(get("/api/products/sku/RAC-CAE-001")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("RAC-CAE-001"));
    }

    @Test
    void shouldReturn404WhenSkuNotFound() throws Exception {
        when(productService.findBySku("XXX-000"))
                .thenThrow(new EntityNotFoundException("Produto não encontrado: XXX-000"));

        mockMvc.perform(get("/api/products/sku/XXX-000")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldSearchProducts() throws Exception {
        when(productService.search("Ração")).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/products/search")
                        .param("term", "Ração")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ração Premium Adulto 15kg"));
    }

    @Test
    void shouldReturnProductsBelowMinStock() throws Exception {
        var belowStock = new ProductResponse(
                PRODUCT_ID, "RAC-CAE-001", "Ração Premium Adulto 15kg",
                "7891234560001", ProductCategory.ALI, AnimalTarget.DOG,
                "Golden", "KG",
                new BigDecimal("85.00"), new BigDecimal("139.90"),
                new BigDecimal("0.3924"),
                new BigDecimal("2.000"), new BigDecimal("5.000"),
                true,
                "A3-P2", "2309.90.00", false, ProductStatus.ACTIVE,
                new SupplierSummary(1L, "FOR-001", "Distribuidora PetBrasil"),
                LocalDateTime.of(2026, 1, 10, 9, 0)
        );

        when(productService.findBelowMinStock()).thenReturn(List.of(belowStock));

        mockMvc.perform(get("/api/products/below-min-stock")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isBelowMinStock").value(true))
                .andExpect(jsonPath("$[0].currentStockQty").value(2.0));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        var updated = new ProductResponse(
                PRODUCT_ID, "RAC-CAE-001", "Ração Premium Adulto 15kg Atualizada",
                "7891234560001", ProductCategory.ALI, AnimalTarget.DOG,
                "Golden", "KG",
                new BigDecimal("90.00"), new BigDecimal("149.90"),
                new BigDecimal("0.3996"),
                new BigDecimal("10.000"), new BigDecimal("5.000"),
                false,
                "A3-P2", "2309.90.00", false, ProductStatus.ACTIVE,
                new SupplierSummary(1L, "FOR-001", "Distribuidora PetBrasil"),
                LocalDateTime.of(2026, 1, 10, 9, 0)
        );

        when(productService.update(eq(PRODUCT_ID), any())).thenReturn(updated);

        mockMvc.perform(put("/api/products/{id}", PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest()))
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ração Premium Adulto 15kg Atualizada"))
                .andExpect(jsonPath("$.salePrice").value(149.90));
    }

    @Test
    void shouldUpdateStock() throws Exception {
        doNothing().when(productService).updateStock(eq(PRODUCT_ID), any());

        mockMvc.perform(patch("/api/products/{id}/stock", PRODUCT_ID)
                        .param("qty", "20.000")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldDeactivateProduct() throws Exception {
        doNothing().when(productService).deactivate(PRODUCT_ID);

        mockMvc.perform(delete("/api/products/{id}", PRODUCT_ID)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeactivatingNonExistentProduct() throws Exception {
        Random random = new Random();
        long unknown = random.nextLong();
        doThrow(new EntityNotFoundException("Produto não encontrado: " + unknown))
                .when(productService).deactivate(unknown);

        mockMvc.perform(delete("/api/products/{id}", unknown)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isNotFound());
    }
}