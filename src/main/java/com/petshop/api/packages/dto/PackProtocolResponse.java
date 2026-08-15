package com.petshop.api.packages.dto;

public record PackProtocolResponse(
        Long id,
        Long protocolId,
        String protocolName,
        String protocolDescription,
        Integer quantity
) {}