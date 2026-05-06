package com.petshop.api.customer.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CoatType {
    SHORT("Curta"),
    MEDIUM("Média"),
    LONG("Longa"),
    CURLY("Caracolada");

    private final String description;
}
