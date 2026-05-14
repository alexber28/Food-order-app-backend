package com.fooddelivery.restaurant_app.controllers;

import com.fooddelivery.restaurant_app.models.Dish;
import com.fooddelivery.restaurant_app.models.Restaurant;
import com.fooddelivery.restaurant_app.repositories.DishRepository;
import com.fooddelivery.restaurant_app.repositories.RestaurantRepository;
import com.fooddelivery.restaurant_app.services.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;
    private final DishRepository dishRepository;
    private final RestaurantRepository restaurantRepository;

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<Dish>> getRestaurantMenu(@PathVariable Long restaurantId) {
        // Получаем данные об авторизации текущего пользователя
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        // Проверяем, есть ли у пользователя права персонала
        boolean isStaff = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));

        if (isStaff) {
            // Персоналу отдаем всё (чтобы можно было включать/выключать)
            return ResponseEntity.ok(dishService.getMenuByRestaurantId(restaurantId));
        } else {
            // Клиентам отдаем только то, что есть в наличии
            return ResponseEntity.ok(dishService.getActiveMenuByRestaurantId(restaurantId));
        }
    }

    // ТОЛЬКО МЕНЕДЖЕР (или АДМИН) может добавлять блюда!
    @PostMapping("/add/{restaurantId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Dish> addDish(@PathVariable Long restaurantId, @RequestBody Dish dish) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Ресторан не найден"));
        
        dish.setRestaurant(restaurant);
        return ResponseEntity.ok(dishRepository.save(dish));
    }

    // UPDATE: Изменить блюдо
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Dish> updateDish(@PathVariable Long id, @RequestBody Dish details) {
        return ResponseEntity.ok(dishService.updateDish(id, details));
    }

    // DELETE: Удалить блюдо
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<String> deleteDish(@PathVariable Long id) {
        dishService.deleteDish(id);
        return ResponseEntity.ok("Блюдо успешно удалено из меню!");
    }
}
