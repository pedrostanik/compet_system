package com.petshop.api.customer.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CatBreed {
    PERSIAN("Persa"),
    SIAMESE("Siamês"),
    ANGORA("Angorá"),
    MAINE_COON("Maine Coon"),
    MIXED_BREED("Vira-lata (SRD)");

    private final String portugueseName;
}