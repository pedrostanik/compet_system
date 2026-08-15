package com.petshop.api.packages.dto;

import java.util.List;

public record PackResponse(
        Long id,
        String name,
        String frequencia,
        List<PackProtocolResponse> protocols
) {}