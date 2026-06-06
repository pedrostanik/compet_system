package com.petshop.api.receipts.dto;

import java.math.BigDecimal;

public record ReceiptItemRequest(
        String description,
        BigDecimal quantity,
        BigDecimal unitPrice,
        Long productId,
        String productSku
) {}