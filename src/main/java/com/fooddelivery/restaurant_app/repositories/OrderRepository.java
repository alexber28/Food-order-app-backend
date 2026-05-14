package com.fooddelivery.restaurant_app.repositories;

import com.fooddelivery.restaurant_app.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId); // История заказов для клиента
    List<Order> findByRestaurantId(Long restaurantId); // Список заказов для менеджера
}
