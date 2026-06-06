package com.petshop.api.receipts.dto;

import java.math.BigDecimal;

public record ReceiptItemResponse(
        Long id,
        String description,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal total,
        Long productId,
        String productSku
) {}