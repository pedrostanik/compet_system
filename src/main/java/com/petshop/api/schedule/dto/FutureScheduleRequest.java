package com.petshop.api.schedule.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record FutureScheduleRequest(
        @NotNull @Valid SchedulingRequest scheduling,
        LocalDateTime time,
        Long packId // required only by POST /future-schedules (checked in SchedulingService)
) {}
