package com.petshop.api.receipts.dto;

import com.petshop.api.receipts.domain.enums.ReceiptType;

import java.math.BigDecimal;
import java.util.List;

public record ReceiptRequest(
        ReceiptType type,
        Long customerId,
        String customerName,
        Long petId,
        String petName,
        Long schedulingId,
        String observations,
        BigDecimal discount,
        List<ReceiptItemRequest> items
) {}