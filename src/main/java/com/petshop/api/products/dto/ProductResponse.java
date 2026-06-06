package com.petshop.api.products.dto;

import com.petshop.api.products.domain.enums.AnimalTarget;
import com.petshop.api.products.domain.enums.ProductCategory;
import com.petshop.api.products.domain.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String barcode,
        ProductCategory category,
        AnimalTarget animalTarget,
        String brand,
        String unit,
        BigDecimal costPrice,
        BigDecimal salePrice,
        BigDecimal margin,
        BigDecimal currentStockQty,
        BigDecimal minStockQty,
        Boolean isBelowMinStock,
        String shelfLocation,
        String ncm,
        boolean loose,
        ProductStatus status,
        SupplierSummary supplier,
        LocalDateTime createdAt
) {}