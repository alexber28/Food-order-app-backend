package com.fooddelivery.restaurant_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.Customizer;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/users/all", "/api/users/*/role", "/api/users/managers").hasRole("ADMIN")
                // Остальные методы в /api/users/ (например, /me, /update) доступны любому авторизованному:
                .requestMatchers("/api/users/**").authenticated()
                .requestMatchers("/api/restaurants/all").hasRole("ADMIN")
                .requestMatchers("/api/restaurants/**").permitAll() // Список ресторанов видят все
                .requestMatchers("/api/dishes/**").permitAll() // Меню видят все
                .requestMatchers("/api/orders/all", "/api/orders/*/status").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/api/orders/**").authenticated() // Доступно всем залогиненным юзерам
                .anyRequest().authenticated() 
            );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Разрешаем запросы только с нашего React-сервера
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        // Разрешаем все методы (GET, POST, PUT, DELETE)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Разрешаем любые заголовки (включая наш Authorization с токеном)
        configuration.setAllowedHeaders(List.of("*"));
        // Разрешаем передавать токен
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}