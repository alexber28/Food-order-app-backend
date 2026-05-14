package com.fooddelivery.restaurant_app.services;

import com.fooddelivery.restaurant_app.models.Dish;
import com.fooddelivery.restaurant_app.repositories.DishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DishService {
    private final DishRepository dishRepository;

    public List<Dish> getMenuByRestaurantId(Long restaurantId) {
        return dishRepository.findByRestaurantId(restaurantId);
    }

    // НОВЫЙ метод для клиентов
    public List<Dish> getActiveMenuByRestaurantId(Long restaurantId) {
        return dishRepository.findByRestaurantIdAndVisibleTrue(restaurantId);
    }

    public Dish updateDish(Long id, Dish updatedDetails) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Блюдо не найдено"));

        dish.setName(updatedDetails.getName());
        dish.setDescription(updatedDetails.getDescription());
        dish.setPrice(updatedDetails.getPrice());
        dish.setImageUrl(updatedDetails.getImageUrl());
        dish.setSectionName(updatedDetails.getSectionName());
        dish.setVisible(updatedDetails.getVisible());

        return dishRepository.save(dish);
    }

    public void deleteDish(Long id) {
        dishRepository.deleteById(id);
    }
}
