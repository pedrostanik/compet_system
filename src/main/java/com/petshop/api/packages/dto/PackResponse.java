package com.petshop.api.packages.dto;

import java.util.List;

public record PackResponse(
        Long id,
        Long petId,
        String petName,
        Long customerId,
        String customerName,
        List<PackProtocolResponse> protocols
) {}