package com.petshop.api.config;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleEntityNotFound(EntityNotFoundException ex) {
        System.err.println("ENTITY NOT FOUND: " + ex.getMessage());
        return ex.getMessage();
    }

    // Captura TUDO que não tem handler específico.
    // Log manual via System.err (não depende do Logback, que está com problema de classpath).
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneric(Exception ex) {
        System.err.println("=== ERRO NAO TRATADO ===");
        System.err.println("Tipo: " + ex.getClass().getName());
        System.err.println("Mensagem: " + ex.getMessage());
        ex.printStackTrace(System.err);
        System.err.println("========================");
        return "Erro interno: " + ex.getClass().getSimpleName() + " - " + ex.getMessage();
    }
}