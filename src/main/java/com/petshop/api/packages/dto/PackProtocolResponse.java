package com.petshop.api.packages.dto;

public record PackProtocolResponse(
        Long protocolId,
        String protocolName,
        String protocolDescription
) {}