package com.petshop.api.products.dto;

import com.petshop.api.products.domain.enums.AnimalTarget;
import com.petshop.api.products.domain.enums.ProductCategory;

import java.math.BigDecimal;

public record ProductRequest(
        String name,
        ProductCategory category,
        AnimalTarget animalTarget,
        String brand,
        String unit,
        BigDecimal costPrice,
        BigDecimal salePrice,
        String barcode,
        BigDecimal minStockQty,
        BigDecimal currentStockQty,
        String shelfLocation,
        String ncm,
        boolean loose,
        String size,
        String color,
        Long supplierId
) {}