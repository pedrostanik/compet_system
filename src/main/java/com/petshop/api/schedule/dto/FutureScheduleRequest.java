package com.petshop.api.schedule.dto;

import java.time.LocalDateTime;

public record FutureScheduleRequest(
        SchedulingRequest scheduling,
        LocalDateTime time,
        Long packId
) {}