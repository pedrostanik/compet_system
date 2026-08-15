package com.petshop.api.customer.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum CatBreed {

    // ===== Vira-lata =====
    MIXED_BREED("Vira-lata (SRD)"),

    // ===== Pelo curto =====
    ABYSSINIAN("Abissínio"),
    AMERICAN_SHORTHAIR("Pelo Curto Americano"),
    AMERICAN_WIREHAIR("Pelo Duro Americano"),
    BENGAL("Bengal"),
    BOMBAY("Bombaim"),
    BRAZILIAN_SHORTHAIR("Pelo Curto Brasileiro"),
    BRITISH_SHORTHAIR("Pelo Curto Inglês"),
    BURMESE("Burmês"),
    BURMILLA("Burmilla"),
    CHARTREUX("Chartreux"),
    COLORPOINT_SHORTHAIR("Colorpoint de Pelo Curto"),
    CORNISH_REX("Cornish Rex"),
    DEVON_REX("Devon Rex"),
    EGYPTIAN_MAU("Mau Egípcio"),
    EUROPEAN_SHORTHAIR("Pelo Curto Europeu"),
    EXOTIC_SHORTHAIR("Exótico de Pelo Curto"),
    HAVANA_BROWN("Havana Brown"),
    JAPANESE_BOBTAIL("Bobtail Japonês"),
    KORAT("Korat"),
    OCICAT("Ocicat"),
    ORIENTAL_SHORTHAIR("Oriental de Pelo Curto"),
    RUSSIAN_BLUE("Azul-Russo"),
    SCOTTISH_FOLD("Scottish Fold"),
    SCOTTISH_STRAIGHT("Scottish Straight"),
    SELKIRK_REX("Selkirk Rex"),
    SIAMESE("Siamês"),
    SINGAPURA("Singapura"),
    SNOWSHOE("Snowshoe"),
    SPHYNX("Sphynx"),
    THAI("Tailandês"),
    TONKINESE("Tonkinês"),

    // ===== Pelo longo / semilongo =====
    ANGORA("Angorá"),
    BALINESE("Balinês"),
    BIRMAN("Sagrado da Birmânia"),
    HIMALAYAN("Himalaio"),
    LAPERM("LaPerm"),
    MAINE_COON("Maine Coon"),
    MUNCHKIN("Munchkin"),
    NEBELUNG("Nebelung"),
    NORWEGIAN_FOREST_CAT("Bosque da Noruega"),
    ORIENTAL_LONGHAIR("Oriental de Pelo Longo"),
    PERSIAN("Persa"),
    RAGAMUFFIN("Ragamuffin"),
    RAGDOLL("Ragdoll"),
    SIBERIAN("Siberiano"),
    SOMALI("Somali"),
    TURKISH_VAN("Van Turco"),

    // ===== Raças exóticas / híbridas =====
    AMERICAN_CURL("American Curl"),
    CYMRIC("Cymric"),
    DONSKOY("Don Sphynx"),
    KHAO_MANEE("Khao Manee"),
    LYKOI("Lykoi"),
    MANX("Manx"),
    NAPOLEON("Napoleon (Minuet)"),
    PETERBALD("Peterbald"),
    PIXIEBOB("Pixie-bob"),
    SAVANNAH("Savannah"),
    SERENGETI("Serengeti"),
    TOYGER("Toyger");

    private final String portugueseName;

    CatBreed(String portugueseName) {
        this.portugueseName = portugueseName;
    }

    @JsonValue
    public String getPortugueseName() {
        return portugueseName;
    }
}