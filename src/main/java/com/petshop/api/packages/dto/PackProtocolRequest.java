package com.petshop.api.packages.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PackProtocolRequest(
    @NotNull Long protocolId,
    String protocolName,
    String protocolDescription,
    @NotNull @Positive Integer quantity
) {}
