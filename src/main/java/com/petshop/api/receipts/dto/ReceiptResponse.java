package com.petshop.api.receipts.dto;

import com.petshop.api.receipts.domain.enums.ReceiptStatus;
import com.petshop.api.receipts.domain.enums.ReceiptType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReceiptResponse(
        Long id,
        String number,
        ReceiptType type,
        ReceiptStatus status,
        Long customerId,
        String customerName,
        Long petId,
        String petName,
        Long schedulingId,
        String observations,
        BigDecimal discount,
        BigDecimal subtotal,
        BigDecimal total,
        List<ReceiptItemResponse> items,
        LocalDateTime createdAt
) {}