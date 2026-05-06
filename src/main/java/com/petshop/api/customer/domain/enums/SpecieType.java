package com.petshop.api.customer.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpecieType {
    CANINE("Canina"),
    FELINE("Felina");

    private final String description;
}
