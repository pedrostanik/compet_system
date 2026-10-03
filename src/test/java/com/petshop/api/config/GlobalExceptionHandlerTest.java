package com.petshop.api.config;

import com.petshop.api.products.service.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new RequestIdFilter())
                // The app's own ObjectMapper, so the RFC 7807 shape (requestId at the root) is what production sends.
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new JacksonConfig().objectMapper()))
                .build();
    }

    @Test
    void unexpectedException_returns500_withoutLeakingInternals() throws Exception {
        mockMvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.requestId").isNotEmpty())
                .andExpect(jsonPath("$.detail", not(containsString("NullPointerException"))))
                .andExpect(jsonPath("$.detail", not(containsString("secret internal detail"))));
    }

    @Test
    void requestId_isEchoedInHeaderAndBody() throws Exception {
        mockMvc.perform(get("/test/boom").header(RequestIdFilter.HEADER, "abc-12345678"))
                .andExpect(header().string(RequestIdFilter.HEADER, "abc-12345678"))
                .andExpect(jsonPath("$.requestId").value("abc-12345678"));
    }

    @Test
    void unsafeIncomingRequestId_isReplaced() throws Exception {
        mockMvc.perform(get("/test/boom").header(RequestIdFilter.HEADER, "bad id\nforged-log-line"))
                .andExpect(header().string(RequestIdFilter.HEADER, not(containsString("forged"))));
    }

    @Test
    void entityNotFound_returns404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Registro não encontrado."))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void businessException_returns422_withItsMessage() throws Exception {
        mockMvc.perform(get("/test/business"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("CNPJ já cadastrado"));
    }

    @Test
    void uniqueViolation_returns409_withoutSql() throws Exception {
        mockMvc.perform(get("/test/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", not(containsString("duplicate key"))));
    }

    @Test
    void notNullViolation_returns400() throws Exception {
        mockMvc.perform(get("/test/not-null"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Dados inválidos ou incompletos."));
    }

    @Test
    void problemDetail_isFlat_noPropertiesWrapper() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(jsonPath("$.properties").doesNotExist())
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void accessDenied_returns403() throws Exception {
        mockMvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden());
    }

    @Test
    void responseStatusException_keepsStatusAndReason_andGetsRequestId() throws Exception {
        mockMvc.perform(get("/test/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Credenciais inválidas"))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void validationFailure_returns400_withFieldErrors() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("must not be blank"))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void malformedJson_returns400_notA500() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    record NamedRequest(@NotBlank String name) {}

    @RestController
    @RequestMapping("/test")
    static class ThrowingController {

        @GetMapping("/boom")
        void boom() {
            throw new NullPointerException("secret internal detail");
        }

        @GetMapping("/not-found")
        void notFound() {
            throw new EntityNotFoundException("Unable to find com.petshop.api.customer.domain.Customer with id 9");
        }

        @GetMapping("/business")
        void business() {
            throw new BusinessException("CNPJ já cadastrado");
        }

        @GetMapping("/duplicate")
        void duplicate() {
            throw new DataIntegrityViolationException("could not execute statement",
                    new SQLException("ERROR: duplicate key value violates unique constraint", "23505"));
        }

        @GetMapping("/not-null")
        void notNull() {
            throw new DataIntegrityViolationException("could not execute statement",
                    new SQLException("ERROR: null value in column \"cpf\"", "23502"));
        }

        @GetMapping("/forbidden")
        void forbidden() {
            throw new AccessDeniedException("Access Denied");
        }

        @GetMapping("/unauthorized")
        void unauthorized() {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        @PostMapping("/validate")
        void validate(@Valid @RequestBody NamedRequest request) {
        }
    }
}
