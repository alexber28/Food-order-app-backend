package com.fooddelivery.restaurant_app.services;

import com.fooddelivery.restaurant_app.models.CartItem;
import com.fooddelivery.restaurant_app.models.Dish;
import com.fooddelivery.restaurant_app.models.User;
import com.fooddelivery.restaurant_app.repositories.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;

    public List<CartItem> getUserCart(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    public void addToCart(User user, Dish dish, int quantity) {
        // Получаем текущую корзину пользователя
        List<CartItem> cart = cartItemRepository.findByUserId(user.getId());

        // Ищем, есть ли уже это блюдо в корзине
        Optional<CartItem> existingItem = cart.stream()
                .filter(item -> item.getDish().getId().equals(dish.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Если блюдо уже есть, просто увеличиваем количество
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            // Если блюда нет, создаем новую запись в корзине
            CartItem newItem = new CartItem();
            newItem.setUser(user);
            newItem.setDish(dish);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
        }
    }

    // Пригодится, когда мы будем превращать корзину в реальный заказ
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    public void removeCartItem(Long cartItemId, Long userId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Позиция в корзине не найдена"));
        
        // Проверяем, что товар реально лежит в корзине того, кто делает запрос
        if (!item.getUser().getId().equals(userId)) {
            throw new RuntimeException("Ошибка: Вы не можете удалить чужой товар!");
        }
        
        cartItemRepository.deleteById(cartItemId);
    }
}
