package com.petshop.api.schedule.dto;

import com.petshop.api.schedule.domain.enums.ScheduleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SchedulingResponse(
        Long id,
        Long packId,
        Long customerId,
        Integer packCycle,
        String customerName,
        Long petId,
        String petName,
        String schedulingObservations,
        LocalDateTime time,
        ScheduleStatus scheduleStatus,
        Boolean isPackage,
        Integer duration,
        Boolean intercepted,
        List<SchedulingProtocolResponse> protocols,
        BigDecimal price
) {
}