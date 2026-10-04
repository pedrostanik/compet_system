package com.petshop.api.protocols.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProtocolRequest(
        // Required: the name is copied into scheduling_protocols.protocol_name, which is NOT NULL.
        @NotBlank @Size(max = 255) String name,
        @Size(max = 255) String description
) {
}
