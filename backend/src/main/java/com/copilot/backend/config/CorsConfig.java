package com.copilot.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // Каже Spring: "Прочитай цей клас під час запуску сервера як налаштування"
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Дозволяємо CORS для всіх ендпоінтів, що починаються на /api/
                .allowedOrigins("http://localhost:5173") // Порт вашого Vite + React
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
