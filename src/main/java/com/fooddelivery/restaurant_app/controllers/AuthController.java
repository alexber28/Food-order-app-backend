package com.fooddelivery.restaurant_app.controllers;

import com.fooddelivery.restaurant_app.security.JwtUtils;
import com.fooddelivery.restaurant_app.models.User;
import com.fooddelivery.restaurant_app.models.enums.Role;
import com.fooddelivery.restaurant_app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    // Регистрация нового пользователя
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }
        // Хешируем пароль перед сохранением
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ROLE_USER); // По умолчанию все - обычные юзеры
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully!");
    }

    // Логин - возвращает JWT токен
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (passwordEncoder.matches(password, user.getPassword())) {
            // Достаем роль нашего пользователя (превращаем в строку на случай, если это Enum)
            String userRole = user.getRole().toString(); 
            
            // Передаем и email, и роль в наш обновленный генератор!
            String token = jwtUtils.generateToken(email, userRole);
            
            return ResponseEntity.ok(Map.of("token", token));
        } else {
            return ResponseEntity.status(401).body("Invalid password!");
        }
    }
}
