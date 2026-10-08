package com.petshop.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173",
                                "http://18.119.7.127",
                                "https://petshopcompet.com.br")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                .allowedHeaders("*")
                // Lets the browser send the refresh cookie when the web app runs on another port (local dev).
                .allowCredentials(true)
                .exposedHeaders("X-Request-Id", "Retry-After");
    }
}