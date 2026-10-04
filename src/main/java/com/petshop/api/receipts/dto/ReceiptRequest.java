package com.petshop.api.receipts.dto;

import com.petshop.api.receipts.domain.enums.ReceiptType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ReceiptRequest(
        @NotNull ReceiptType type,
        Long customerId,
        @Size(max = 255) String customerName,
        Long petId,
        @Size(max = 255) String petName,
        Long schedulingId,
        @Size(max = 500) String observations,
        @PositiveOrZero BigDecimal discount,
        @NotEmpty List<@NotNull @Valid ReceiptItemRequest> items
) {}
