package com.petshop.api.packages.dto;

public record PackProtocolRequest(
    Long protocolId,
    String protocolName,
    String protocolDescription,
    Integer quantity
) {}