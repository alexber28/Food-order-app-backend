package com.fooddelivery.restaurant_app.services;

import com.fooddelivery.restaurant_app.controllers.RestaurantController;
import com.fooddelivery.restaurant_app.models.Restaurant;
import com.fooddelivery.restaurant_app.models.User;
import com.fooddelivery.restaurant_app.repositories.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fooddelivery.restaurant_app.repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    // Метод для обычных юзеров: показываем только те рестораны, которые не скрыты
    public List<Restaurant> getAllVisibleRestaurants() {
        return restaurantRepository.findByVisibleTrue();
    }

    // Метод для админа: сохранение нового ресторана
    public Restaurant saveRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    public Restaurant createRestaurant(RestaurantController.RestaurantCreateRequest request) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.name());
        restaurant.setDescription(request.description());
        restaurant.setVisible(request.visible());

        // Если админ передал ID менеджера, находим его в базе и привязываем
        if (request.managerId() != null) {
            User manager = userRepository.findById(request.managerId())
                    .orElseThrow(() -> new RuntimeException("Менеджер с таким ID не найден"));
            restaurant.setManager(manager);
        }

        return restaurantRepository.save(restaurant);
    }

    // Метод для обновления ресторана
    public Restaurant updateRestaurant(Long id, Restaurant updatedDetails) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ресторан не найден"));

        restaurant.setName(updatedDetails.getName());
        restaurant.setDescription(updatedDetails.getDescription());
        restaurant.setVisible(updatedDetails.getVisible());
        restaurant.setImageUrl(updatedDetails.getImageUrl());

        // ЛОГИКА ПРИВЯЗКИ МЕНЕДЖЕРА
        if (updatedDetails.getManager() != null && updatedDetails.getManager().getId() != null) {
            User manager = userRepository.findById(updatedDetails.getManager().getId())
                    .orElseThrow(() -> new RuntimeException("Менеджер не найден"));
            restaurant.setManager(manager);
        } else {
            restaurant.setManager(null); // Если менеджер не выбран
        }

        return restaurantRepository.save(restaurant);
    }

    // Метод для удаления ресторана
    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }
}
