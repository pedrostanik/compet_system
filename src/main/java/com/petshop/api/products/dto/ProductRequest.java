package com.petshop.api.products.dto;

import com.petshop.api.products.domain.enums.AnimalTarget;
import com.petshop.api.products.domain.enums.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 255) String name,
        @NotNull ProductCategory category,
        @NotNull AnimalTarget animalTarget,
        @Size(max = 255) String brand,
        @NotBlank @Size(max = 10) String unit,
        @NotNull @PositiveOrZero BigDecimal costPrice,
        @NotNull @PositiveOrZero BigDecimal salePrice,
        @Size(max = 20) String barcode,
        @PositiveOrZero BigDecimal minStockQty,
        @PositiveOrZero BigDecimal currentStockQty,
        @Size(max = 255) String shelfLocation,
        @Size(max = 10) String ncm,
        boolean loose,
        @Size(max = 255) String size,
        @Size(max = 255) String color,
        Long supplierId
) {}
