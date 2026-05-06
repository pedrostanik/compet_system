package com.petshop.api.customer.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


    @Getter
    @RequiredArgsConstructor
    public enum DogBreed {
        MIXED_BREED("Vira-lata"),
        SHIH_TZU("Shih Tzu"),
        GERMAN_SPITZ("Spitz Alemão"),
        YORKSHIRE_TERRIER("Yorkshire"),
        LHASA_APSO("Lhasa Apso"),
        PUG("Pug"),
        PINSCHER("Pinscher"),
        DACHSHUND("Dachshund (Salsicha)"),
        GOLDEN_RETRIEVER("Golden Retriever"),
        LABRADOR_RETRIEVER("Labrador"),
        GERMAN_SHEPHERD("Pastor Alemão"),
        BORDER_COLLIE("Border Collie"),
        ROTTWEILER("Rottweiler");

        private final String portugueseName;
    }

