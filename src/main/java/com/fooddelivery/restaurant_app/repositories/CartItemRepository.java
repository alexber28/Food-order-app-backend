package com.fooddelivery.restaurant_app.repositories;

import com.fooddelivery.restaurant_app.models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(Long userId); // Получить корзину пользователя
    void deleteByUserId(Long userId); // Очистить корзину после оформления заказа
}
