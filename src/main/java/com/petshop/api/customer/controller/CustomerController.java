package com.petshop.api.customer.controller;

import com.petshop.api.customer.domain.enums.CatBreed;
import com.petshop.api.customer.domain.enums.CoatType;
import com.petshop.api.customer.domain.enums.DogBreed;
import com.petshop.api.customer.domain.enums.SpecieType;
import com.petshop.api.customer.dto.CustomerRequest;
import com.petshop.api.customer.dto.CustomerResponse;
import com.petshop.api.customer.dto.PetRequest;
import com.petshop.api.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;


    @GetMapping("/species")
    public List<Map<String, String>> species() {
        return Arrays.stream(SpecieType.values())
                .map(e -> Map.of("value", e.name(), "label", e.getDescription()))
                .toList();
    }

    @GetMapping("/coat-types")
    public List<Map<String, String>> coatTypes() {
        return Arrays.stream(CoatType.values())
                .map(e -> Map.of("value", e.name(), "label", e.getDescription()))
                .toList();
    }

    @GetMapping("/cat-breeds")
    public List<Map<String, String>> catBreeds() {
        return Arrays.stream(CatBreed.values())
                .map(e -> Map.of("value", e.name(), "label", e.getPortugueseName()))
                .toList();
    }

    @GetMapping("/dogs-breeds")
    public List<Map<String, String>> dogBreeds() {
        return Arrays.stream(DogBreed.values())
                .map(e -> Map.of("value", e.name(), "label", e.getPortugueseName()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@RequestBody CustomerRequest request) {
        return customerService.createCustomer(request);
    }

    @GetMapping
    public List<CustomerResponse> listCustomers() {
        return customerService.listCustomers();
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable Long id) {
        return customerService.getCustomer(id);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id, @RequestBody CustomerRequest request) {
        return customerService.updateCustomer(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
    }

    @PostMapping("/{id}/pets")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse addPet(@PathVariable Long id, @RequestBody PetRequest request) {
        return customerService.addPet(id, request);
    }

    @DeleteMapping("/{customerId}/pets/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePet(@PathVariable Long customerId, @PathVariable Long petId) {
        customerService.removePet(customerId, petId);
    }

    @PutMapping("/{customerId}/pets/{petId}")
    public CustomerResponse updatePet(@PathVariable Long customerId, @PathVariable Long petId, @RequestBody PetRequest request) {
        return customerService.updatePet(customerId, petId, request);
    }
}
