package com.petshop.api.customer.service;

import com.petshop.api.customer.domain.Customer;
import com.petshop.api.customer.domain.Pet;
import com.petshop.api.customer.dto.CustomerRequest;
import com.petshop.api.customer.dto.CustomerResponse;
import com.petshop.api.customer.dto.PetRequest;
import com.petshop.api.customer.dto.PetResponse;
import com.petshop.api.customer.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        var customer = new Customer();
        updateCustomerFields(customer, request);
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse addPet(Long customerId, PetRequest request) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        var pet = new Pet();
        mapPetRequestToEntity(pet, request);
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

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        var customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        updateCustomerFields(customer, request);
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new EntityNotFoundException("Customer not found");
        }
        customerRepository.deleteById(id);
    }

    @Transactional
    public void removePet(Long customerId, Long petId) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        customer.getPets().removeIf(p -> p.getId().equals(petId));
        customerRepository.save(customer);
    }

    @Transactional
    public CustomerResponse updatePet(Long customerId, Long petId, PetRequest request) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        var pet = customer.getPets().stream()
                .filter(p -> p.getId().equals(petId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Pet not found"));

        mapPetRequestToEntity(pet, request);
        return toResponse(customerRepository.save(customer));
    }

    // --- MÉTODOS AUXILIARES DE MAPEAMENTO ---

    private void updateCustomerFields(Customer customer, CustomerRequest request) {
        customer.setName(request.name());
        customer.setPhone(request.phone());
        customer.setCpf(request.cpf());
        customer.setEmail(request.email());
        // Se tiver endereço no request, adicione aqui: customer.setAddress(request.address());
    }

    private void mapPetRequestToEntity(Pet pet, PetRequest request) {
        pet.setName(request.name());
        pet.setAge(request.age());
        pet.setSpecies(request.species());
        pet.setRace(request.race()); // Usando breed do record que corrigimos
        pet.setCoatType(request.coatType());
        pet.setWeight(request.weight());

        // Vacinas e Saúde
        pet.setRabieVaccination(request.rabieVaccination());
        pet.setRabieVaccinationDate(request.rabieVaccinationDate());
        pet.setV10Vaccination(request.v10Vaccination());
        pet.setV10VaccinationDate(request.v10VaccinationDate());
        pet.setDewormed(request.dewormed());
        pet.setDewormedDate(request.dewormedDate());

        pet.setAllergy(request.allergy());
        pet.setHealthIssues(request.healthIssues());
        pet.setObservations(request.observations());
    }

    private CustomerResponse toResponse(Customer c) {
        List<PetResponse> petDtos = c.getPets().stream()
                .map(p -> new PetResponse(
                        p.getId(),
                        p.getName(),
                        p.getAge(),
                        p.getSpecies() != null ? p.getSpecies() : null,
                        p.getRace(),
                        p.getRabieVaccination(),
                        p.getRabieVaccinationDate(),
                        p.getV10Vaccination(),
                        p.getV10VaccinationDate(),
                        p.getDewormed(),
                        p.getDewormedDate(),
                        p.getAllergy(),
                        p.getHealthIssues(),
                        p.getWeight(),
                        p.getCoatType(),
                        p.getObservations()
                ))
                .toList();

        return new CustomerResponse(
                c.getId(),
                c.getName(),
                c.getPhone(),
                c.getCpf(),
                c.getEmail(),
                c.getAddress(),
                petDtos // Agora passamos a lista de DTOs, não de Entidades
        );
    }
}