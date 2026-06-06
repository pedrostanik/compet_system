package com.petshop.api.customer.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum CatBreed {
    PERSIAN("Persa"),
    SIAMESE("Siamês"),
    ANGORA("Angorá"),
    MAINE_COON("Maine Coon"),
    MIXED_BREED("Vira-lata (SRD)");

    private final String portugueseName;

    CatBreed(String portugueseName) {
        this.portugueseName = portugueseName;
    }

    @JsonValue
    public String getPortugueseName() {
        return portugueseName;
    }
}