package com.petshop.api.products.dto;

import javax.validation.constraints.Pattern;

public record SupplierResponse(
        Long id,
        String code,
        String name,
        @Pattern(regexp = "\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}")
        String cnpj,
        String contactName,
        String phone,
        String email,
        Integer avgLeadTimeDays,
        Integer minOrderQty,
        boolean active,
        int totalProducts
) {}
