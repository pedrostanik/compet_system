package com.petshop.api.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petshop.api.auth.filter.JwtFilter;
import com.petshop.api.customer.domain.enums.CoatType;
import com.petshop.api.customer.domain.enums.SpecieType;
import com.petshop.api.customer.dto.CustomerRequest;
import com.petshop.api.customer.dto.CustomerResponse;
import com.petshop.api.customer.dto.PetRequest;
import com.petshop.api.customer.dto.PetResponse;
import com.petshop.api.customer.service.CustomerService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerResponse buildCustomerResponse() {
        // 1. Criamos o PetResponse com todos os novos campos
        var petResponse = new PetResponse(
                1L,                             // id
                "Nox",                          // name
                3,                              // age
                SpecieType.FELINE,              // species (Enum)
                "SRD",                          // race/breed
                true,                           // rabiesVaccination
                LocalDateTime.now(),            // rabiesVaccinationDate
                true,                           // v10Vaccination
                LocalDateTime.now(),            // v10VaccinationDate
                true,                           // dewormed
                LocalDateTime.now(),            // dewormedDate
                "Nenhuma",                      // allergy
                "Saudável",                     // healthIssues
                4.5f,                           // weight
                CoatType.SHORT,                 // coatType (Enum)
                "Observação teste"              // observations
        );

        // 2. Criamos o CustomerResponse com endereço e a lista de PetResponse
        return new CustomerResponse(
                1L,                             // id
                "Pedro Ostanik",                // name
                "(11) 99999-9999",              // phone
                "123.456.789-00",               // cpf
                "pedro@email.com",              // email
                "Rua Exemplo, 123",             // address (adicionado no record)
                List.of(petResponse)            // pets (List<PetResponse>)
        );
    }

    private CustomerRequest buildCustomerRequest() {
        return new CustomerRequest("Pedro Ostanik", "11 99999-9999",
                "12431241243", "pedro@email.com", "Rua" );
    }

    private PetRequest buildPetRequest() {
        return new PetRequest(
                "Nox",                          // name
                3,                              // age (Integer)
                SpecieType.FELINE,              // species (Enum)
                "SRD",                          // breed/race (String)
                true,                           // rabiesVaccination
                LocalDateTime.now(),            // rabiesVaccinationDate
                true,                           // v10Vaccination
                LocalDateTime.now(),            // v10VaccinationDate
                true,                           // dewormed
                LocalDateTime.now(),            // dewormedDate
                "Nenhuma",                      // allergy
                "Saudável",                     // healthIssues
                4.5f,                           // weight (Float)
                CoatType.SHORT,                 // coatType (Enum)
                "Observação de teste"
        );
    }

    @Test
    void shouldCreateCustomer() throws Exception {
        when(customerService.createCustomer(any())).thenReturn(buildCustomerResponse());

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCustomerRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Pedro Ostanik"))
                .andExpect(jsonPath("$.email").value("pedro@email.com"));
    }

    @Test
    void shouldListCustomers() throws Exception {
        when(customerService.listCustomers()).thenReturn(List.of(buildCustomerResponse()));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Pedro Ostanik"));
    }

    @Test
    void shouldGetCustomerById() throws Exception {
        when(customerService.getCustomer(1L)).thenReturn(buildCustomerResponse());

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.pets[0].name").value("Nox"));
    }

    @Test
    void shouldReturn404WhenCustomerNotFound() throws Exception {
        when(customerService.getCustomer(99L)).thenThrow(new EntityNotFoundException("Customer not found"));

        mockMvc.perform(get("/api/customers/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        CustomerResponse updated = buildCustomerResponse();
        when(customerService.updateCustomer(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCustomerRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pedro Ostanik"));
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        doNothing().when(customerService).deleteCustomer(1L);

        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldAddPetToCustomer() throws Exception {
        when(customerService.addPet(eq(1L), any())).thenReturn(buildCustomerResponse());

        mockMvc.perform(post("/api/customers/1/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildPetRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pets[0].name").value("Nox"));
    }

    @Test
    void shouldUpdatePet() throws Exception {
        when(customerService.updatePet(eq(1L), eq(1L), any())).thenReturn(buildCustomerResponse());

        mockMvc.perform(put("/api/customers/1/pets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildPetRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pets[0].name").value("Nox"));
    }

    @Test
    void shouldRemovePet() throws Exception {
        doNothing().when(customerService).removePet(1L, 1L);

        mockMvc.perform(delete("/api/customers/1/pets/1"))
                .andExpect(status().isNoContent());
    }
}