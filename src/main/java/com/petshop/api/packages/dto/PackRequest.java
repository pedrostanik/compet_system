package com.petshop.api.packages.dto;

import java.util.List;

public record PackRequest(
        String name,
        String frequencia,
        List<PackProtocolRequest>protocols
) {}




