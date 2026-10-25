package com.example.parking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;
    private final String timezone;

    public AppConfig(@Value("${app.cors.allowed-origins}") String[] allowedOrigins,
                     @Value("${app.timezone}") String timezone) {
        this.allowedOrigins = allowedOrigins;
        this.timezone = timezone;
    }

    /** One clock for the whole app: the same timezone is used for "today" and for revenue dates. */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of(timezone));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PATCH", "OPTIONS");
    }
}
