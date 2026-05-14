package com.fooddelivery.restaurant_app.controllers;

import com.fooddelivery.restaurant_app.models.Restaurant;
import com.fooddelivery.restaurant_app.repositories.UserRepository;
import com.fooddelivery.restaurant_app.models.User;
import com.fooddelivery.restaurant_app.services.RestaurantService;
import com.fooddelivery.restaurant_app.repositories.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    // Этот метод могут дергать все
    @GetMapping
    public List<Restaurant> getAllVisibleRestaurants() {
        return restaurantService.getAllVisibleRestaurants();
    }

    // Эндпоинт для АДМИНА: получить ВООБЩЕ ВСЕ рестораны (даже скрытые)
    @GetMapping("/all")
    public ResponseEntity<List<Restaurant>> getAllRestaurants() {
        // Метод findAll() достает из базы абсолютно все записи без фильтрации
        List<Restaurant> allRestaurants = restaurantRepository.findAll();
        return ResponseEntity.ok(allRestaurants);
    }

    // Метод для получения одного конкретного ресторана по его ID
    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getRestaurantById(@PathVariable Long id) {
        return restaurantRepository.findById(id)
                .map(ResponseEntity::ok) // Если нашли - отдаем 200 OK и данные
                .orElse(ResponseEntity.notFound().build()); // Если нет - 404
    }

    // Наш DTO - содержит только то, что реально нужно ввести админу руками
    public record RestaurantCreateRequest(String name, String description, Long managerId, boolean visible) {}

    // Обновленный эндпоинт создания
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Restaurant> createRestaurant(@RequestBody RestaurantCreateRequest request) {
        return ResponseEntity.ok(restaurantService.createRestaurant(request));
    }
    
    // UPDATE: Изменить данные ресторана (PUT http://localhost:8080/api/restaurants/{id})
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Restaurant> updateRestaurant(
            @PathVariable Long id, 
            @RequestBody Restaurant restaurantDetails,
            // Добавляем AuthenticationPrincipal, чтобы узнать, кто делает запрос
            @AuthenticationPrincipal UserDetails userDetails 
    ) {
        Restaurant existingRestaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ресторан не найден"));

        // Эти поля могут менять и Админ, и Менеджер
        existingRestaurant.setName(restaurantDetails.getName());
        existingRestaurant.setDescription(restaurantDetails.getDescription());
        existingRestaurant.setImageUrl(restaurantDetails.getImageUrl());
        existingRestaurant.setVisible(restaurantDetails.getVisible());

        // Проверяем, есть ли у текущего юзера роль ADMIN
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        // ТОЛЬКО АДМИН имеет право менять или удалять менеджера
        if (isAdmin) {
            existingRestaurant.setManager(restaurantDetails.getManager());
        }

        Restaurant updatedRestaurant = restaurantRepository.save(existingRestaurant);
        return ResponseEntity.ok(updatedRestaurant);
    }

    // DELETE: Удалить ресторан (DELETE http://localhost:8080/api/restaurants/{id})
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.ok("Ресторан успешно удален!");
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Restaurant> getMyRestaurant(@AuthenticationPrincipal UserDetails userDetails) {
        // Ищем менеджера по email из токена
        User manager = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        // Ищем ресторан, где manager_id равен ID этого юзера
        // ВАЖНО: убедись, что в RestaurantRepository есть метод findByManagerId(Long managerId);
        Restaurant restaurant = restaurantRepository.findByManagerId(manager.getId())
                .orElseThrow(() -> new RuntimeException("У вас еще нет привязанного ресторана!"));
        
        return ResponseEntity.ok(restaurant);
    }
}
