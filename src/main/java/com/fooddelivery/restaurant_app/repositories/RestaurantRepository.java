package com.fooddelivery.restaurant_app.repositories;

import com.fooddelivery.restaurant_app.models.Restaurant;
//import com.fooddelivery.restaurant_app.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByVisibleTrue(); // Чтобы отдавать клиентам только видимые рестораны
    Optional<Restaurant> findByManagerId(Long managerId);
}
