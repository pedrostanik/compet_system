package com.petshop.api.schedule.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SchedulingRequest(
        Long packId,
        Long customerId,
        Integer packCycle,
        String customerName,
        Long petId,
        String petName,
        String schedulingObservations,
        LocalDateTime time,
        Boolean isPackage,
        Integer duration,
        List<Long> protocolIds,
        BigDecimal price
) {
}

