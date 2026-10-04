package com.petshop.api.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // <-- Importante
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.ProblemDetailJacksonMixin;

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

        // "" em campos de enum/objeto vira null (os formulários mandam "" quando nada foi escolhido),
        // e a Bean Validation responde com o campo exato (@NotNull) em vez de um erro de leitura do JSON.
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        // 3. Seu módulo customizado para o trim de Strings
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new TrimStringDeserializer());
        mapper.registerModule(module);

        // 4. Serializa ProblemDetail (respostas de erro) no formato RFC 7807, com as propriedades
        //    extras (ex.: requestId) no nível raiz e não dentro de "properties". O Spring só faz isso
        //    sozinho no ObjectMapper dele; como este bean o substitui, o mixin é registrado aqui.
        mapper.addMixIn(ProblemDetail.class, ProblemDetailJacksonMixin.class);

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