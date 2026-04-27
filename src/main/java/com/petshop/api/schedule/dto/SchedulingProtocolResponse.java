package com.petshop.api.schedule.dto;

import java.math.BigDecimal;

public record SchedulingProtocolResponse(
        Long protocolId,
        String protocolName,
        BigDecimal protocolPrice
) {}