package com.medibook.medibook_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.Collections;

/**
 * CORS Configuration for Frontend Integration
 */
@Configuration
public class CorsConfig {

        @Bean
        public WebMvcConfigurer corsConfigurer() {
                return new WebMvcConfigurer() {
                        @Override
                        public void addCorsMappings(CorsRegistry registry) {
                                registry.addMapping("/**")
                                                .allowedOrigins("*") // Allow all origins
                                                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                                                .allowedHeaders("*")
                                                .exposedHeaders("Authorization")
                                                .allowCredentials(false)
                                                .maxAge(3600);
                        }
                };
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // Allow all origins (since credentials are false)
                configuration.setAllowedOrigins(Collections.singletonList("*"));

                // Allow all HTTP methods
                configuration.setAllowedMethods(Arrays.asList(
                                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

                // Allow all headers
                configuration.setAllowedHeaders(Collections.singletonList("*"));

                // Expose headers
                configuration.setExposedHeaders(Arrays.asList("Authorization"));

                // Allow credentials (must be false if origin is *)
                configuration.setAllowCredentials(false);

                // Max age
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}
