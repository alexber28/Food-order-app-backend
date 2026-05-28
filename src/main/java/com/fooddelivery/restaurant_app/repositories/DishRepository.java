package com.fooddelivery.restaurant_app.repositories;

import com.fooddelivery.restaurant_app.models.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DishRepository extends JpaRepository<Dish, Long> {
    List<Dish> findByRestaurantId(Long restaurantId); // Получить всё меню конкретного ресторана

    List<Dish> findByRestaurantIdAndVisibleTrue(Long restaurantId);
}