package com.petshop.api.customer.controller;

import com.petshop.api.customer.domain.enums.CatBreed;
import com.petshop.api.customer.domain.enums.CoatType;
import com.petshop.api.customer.domain.enums.DogBreed;
import com.petshop.api.customer.domain.enums.SpecieType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enums")
public class EnumController {

    @GetMapping("/species")
    public List<Map<String, String>> species() {
        return Arrays.stream(SpecieType.values())
                .map(e -> Map.of("value", e.getDescription(), "label", e.getDescription()))
                .toList();
    }

    @GetMapping("/coat-types")
    public List<Map<String, String>> coatTypes() {
        return Arrays.stream(CoatType.values())
                .map(e -> Map.of("value", e.getDescription(), "label", e.getDescription()))
                .toList();
    }

    @GetMapping("/cat-breeds")
    public List<Map<String, String>> catBreeds() {
        return Arrays.stream(CatBreed.values())
                .map(e -> Map.of("value", e.getPortugueseName(), "label", e.getPortugueseName()))
                .toList();
    }

    @GetMapping("/dog-breeds")
    public List<Map<String, String>> dogBreeds() {
        return Arrays.stream(DogBreed.values())
                .map(e -> Map.of("value", e.getPortugueseName(), "label", e.getPortugueseName()))
                .toList();
    }
}
