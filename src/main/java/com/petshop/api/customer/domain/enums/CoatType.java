package com.petshop.api.customer.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum CoatType {
    SHORT("Curta"),
    MEDIUM("Média"),
    LONG("Longa"),
    CURLY("Caracolada");

    private final String description;

    CoatType(String description) {
        this.description = description;
    }

    @JsonValue
    public String getDescription() {
        return description;
    }
}
