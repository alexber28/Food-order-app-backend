package com.fooddelivery.restaurant_app.controllers;

// Импорты твоих моделей и репозиториев
import com.fooddelivery.restaurant_app.models.User;
import com.fooddelivery.restaurant_app.models.enums.Role;
import com.fooddelivery.restaurant_app.repositories.UserRepository;

// Импорты Spring Web
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Импорты Spring Security
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

// Стандартные классы Java
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173") // Для работы с Vite
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. Получить данные текущего пользователя
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return ResponseEntity.ok(user);
    }

    // 2. Обновить данные (имя, фамилия, телефон)
    @PutMapping("/update")
    public ResponseEntity<User> updateProfile(@AuthenticationPrincipal UserDetails userDetails, @RequestBody User updateData) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        
        user.setFirstName(updateData.getFirstName());
        user.setLastName(updateData.getLastName());
        user.setPhoneNumber(updateData.getPhoneNumber());
        
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, String> passwords) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        
        String oldPassword = passwords.get("oldPassword");
        String newPassword = passwords.get("newPassword");

        // Проверяем, совпадает ли старый пароль
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body("Старый пароль введен неверно");
        }

        // Хешируем и сохраняем новый
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return ResponseEntity.ok("Пароль успешно изменен");
    }

    // 3. Магическое пополнение баланса
    @PostMapping("/topup")
    public ResponseEntity<User> topUp(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, BigDecimal> request) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        BigDecimal amount = request.get("amount");
        
        user.setBalance(user.getBalance().add(amount));
        return ResponseEntity.ok(userRepository.save(user));
    }

    // 4. Получить список всех пользователей
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // 5. Изменить роль пользователя
    @PutMapping("/{id}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        String newRoleString = request.get("role");
        
        try {
            // Превращаем строку из JSON в твой Enum. 
            user.setRole(Role.valueOf(newRoleString));
            userRepository.save(user);
            
            return ResponseEntity.ok("Роль успешно обновлена");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Недопустимая роль: " + newRoleString);
        }
    }

    @GetMapping("/managers")
    public ResponseEntity<List<User>> getManagers() {
        return ResponseEntity.ok(userRepository.findByRole(Role.ROLE_MANAGER));
    }
}
