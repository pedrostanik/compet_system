package com.petshop.api.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // <-- Importante
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 1. Registra o suporte a tipos de Data/Hora do Java 8 (LocalDate, LocalDateTime, etc.)
        mapper.registerModule(new JavaTimeModule());

        // Evita que datas sejam serializadas como timestamps numéricos (opcional, mas recomendado)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 2. Outras configurações que você queira manter
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 3. Seu módulo customizado para o trim de Strings
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new TrimStringDeserializer());
        mapper.registerModule(module);

        return mapper;
    }

    public static class TrimStringDeserializer extends com.fasterxml.jackson.databind.JsonDeserializer<String> {
        @Override
        public String deserialize(com.fasterxml.jackson.core.JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            if (value != null) {
                String trimmed = value.trim();
                return trimmed.isEmpty() ? null : trimmed;
            }
            return null;
        }
    }
}