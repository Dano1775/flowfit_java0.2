package com.example.flowfit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad de Spring Security
 * 
 * Esta clase configura:
 * 1. PasswordEncoder (BCrypt) para encriptar contraseñas
 * 2. Deshabilita la autenticación automática de Spring Security
 *    (permitimos acceso sin login a todas las rutas)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Bean de PasswordEncoder usando BCrypt
     * Este encoder se inyecta en PasswordResetService y otros servicios
     * para encriptar contraseñas de forma segura
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuración de seguridad HTTP
     * Deshabilitamos la protección por defecto de Spring Security
     * para permitir acceso libre a todas las rutas
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Permitir acceso a todas las rutas sin autenticación
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // Deshabilitar CSRF (necesario para APIs REST)
            .csrf(csrf -> csrf.disable())
            // Deshabilitar el formulario de login por defecto
            .formLogin(form -> form.disable())
            // Deshabilitar logout por defecto
            .logout(logout -> logout.disable())
            // Deshabilitar HTTP Basic authentication
            .httpBasic(basic -> basic.disable());
        
        return http.build();
    }
}
