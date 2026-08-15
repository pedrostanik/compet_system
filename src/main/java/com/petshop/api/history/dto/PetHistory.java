package com.petshop.api.history.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PetHistory(LocalDateTime time, BigDecimal price, Integer duration, String observations) {
}