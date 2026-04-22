package com.petshop.api.customer.service;

import com.petshop.api.customer.domain.Customer;
import com.petshop.api.customer.domain.Pet;
import com.petshop.api.customer.dto.CustomerRequest;
import com.petshop.api.customer.dto.CustomerResponse;
import com.petshop.api.customer.dto.PetRequest;
import com.petshop.api.customer.dto.PetResponse;
import com.petshop.api.customer.repository.CustomerRepository;
import com.petshop.api.customer.repository.PetRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PetRepository petRepository;

    public CustomerResponse createCustomer(CustomerRequest request) {
        var customer = new Customer();
        customer.setName(request.name());
        customer.setPhone(request.phone());
        customer.setCpf(request.cpf());
        customer.setEmail(request.email());

        return toResponse(customerRepository.save(customer));
    }

    public CustomerResponse addPet(Long customerId, PetRequest request) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        var pet = new Pet();
        pet.setName(request.name());
        pet.setSpecies(request.species());
        pet.setAge(request.age());
        pet.setSpecies(request.species());
        pet.setRace((request.race()));
        pet.setCustomer(customer);

        customer.getPets().add(pet);
        return toResponse(customerRepository.save(customer));
    }

    public CustomerResponse getCustomer(Long id) {
        return customerRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
    }

    public List<CustomerResponse> listCustomers() {
        return customerRepository.findAll().stream()
                .map(this::toResponse).toList();
    }

    private CustomerResponse toResponse(Customer c) {
        var pets = c.getPets().stream()

                .map(p -> new PetResponse(p.getId(), p.getName(), p.getAge(),
                        p.getSpecies(), p.getRace(), p.getObservations(), p.getCustomer() ))
                .toList();
        return new CustomerResponse(c.getId(), c.getName(),  c.getPhone(), c.getCpf(), c.getEmail(), c.getPets());
    }

    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        var customer = customerRepository.findByIdWithPets(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        customer.setName(request.name());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        return toResponse(customerRepository.save(customer));
    }

    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new EntityNotFoundException("Customer not found");
        }
        customerRepository.deleteById(id);
    }

    public void removePet(Long customerId, Long petId) {
        var customer = customerRepository.findByIdWithPets(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        customer.getPets().removeIf(p -> p.getId() == petId);
        customerRepository.save(customer);
    }

    public CustomerResponse updatePet(Long customerId, Long petId, PetRequest request) {
        var customer = customerRepository.findByIdWithPets(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        var pet = customer.getPets().stream()
                .filter(p -> p.getId().equals(petId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Pet not found"));
        pet.setName(request.name());
        pet.setAge(request.age());
        pet.setSpecies(request.species());
        pet.setRace(request.race());
        pet.setObservations(request.observations());
        return toResponse(customerRepository.save(customer));
    }
}