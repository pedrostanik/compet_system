package com.petshop.api.packages.dto;

import java.util.List;

public record PackRequest(
        Long petId,
        String petName,
        Long customerId,
        String customerName,
        List<Long> protocolIds
) {}




