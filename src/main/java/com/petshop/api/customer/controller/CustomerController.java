package com.petshop.api.customer.controller;

import com.petshop.api.customer.dto.CustomerRequest;
import com.petshop.api.customer.dto.CustomerResponse;
import com.petshop.api.customer.dto.PetRequest;
import com.petshop.api.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

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
}
