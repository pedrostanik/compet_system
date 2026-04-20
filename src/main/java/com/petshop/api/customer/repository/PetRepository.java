package com.petshop.api.customer.repository;

import com.petshop.api.customer.domain.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findByCustomerId(Long customerId);

    List<Pet> findByCustomerIdAndSpeciesIgnoreCase(Long customerId, String species);

    List<Pet> findByNameContainingIgnoreCase(String name);
}