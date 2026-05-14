package com.fooddelivery.restaurant_app.services;

import com.fooddelivery.restaurant_app.models.*;
import com.fooddelivery.restaurant_app.models.enums.OrderStatus;
import com.fooddelivery.restaurant_app.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    //private final RestaurantRepository restaurantRepository;

    @Transactional
    public List<Order> createOrdersFromCart(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        List<CartItem> cart = cartItemRepository.findByUserId(userId);
        if (cart.isEmpty()) {
            throw new RuntimeException("Корзина пуста, заказ невозможен");
        }

        // 1. Считаем общую (гранд) сумму всей корзины для проверки баланса
        BigDecimal grandTotal = cart.stream()
                .map(item -> item.getDish().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Проверяем баланс
        if (user.getBalance().compareTo(grandTotal) < 0) {
            throw new RuntimeException("Недостаточно средств на балансе! Пополните счет.");
        }
        
        // Списываем деньги один раз за всю корзину
        user.setBalance(user.getBalance().subtract(grandTotal));
        userRepository.save(user);

        // 3. ГРУППИРУЕМ ТОВАРЫ ПО РЕСТОРАНАМ
        // Получаем Map, где ключ - Ресторан, а значение - список товаров в корзине из этого ресторана
        Map<Restaurant, List<CartItem>> itemsByRestaurant = cart.stream()
                .collect(Collectors.groupingBy(item -> item.getDish().getRestaurant()));

        List<Order> createdOrders = new ArrayList<>();

        // 4. Создаем отдельный заказ для каждого ресторана
        for (Map.Entry<Restaurant, List<CartItem>> entry : itemsByRestaurant.entrySet()) {
            Restaurant restaurant = entry.getKey();
            List<CartItem> restaurantItems = entry.getValue();

            // Считаем сумму только для конкретного ресторана
            BigDecimal restaurantTotal = restaurantItems.stream()
                    .map(item -> item.getDish().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Order order = new Order();
            order.setUser(user);
            order.setRestaurant(restaurant);
            order.setStatus(OrderStatus.CREATED);
            order.setWaitTime(0); // Ставим 0 по умолчанию. Менеджер установит реальное время позже.
            order.setTotalPrice(restaurantTotal);
            Order savedOrder = orderRepository.save(order);

            // Сохраняем позиции для этого заказа
            List<OrderItem> orderItems = restaurantItems.stream().map(item -> {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(savedOrder);
                orderItem.setDish(item.getDish());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setPriceAtPurchase(item.getDish().getPrice());
                return orderItem;
            }).toList();
            orderItemRepository.saveAll(orderItems);

            createdOrders.add(savedOrder);
        }

        // 5. Очищаем корзину после успешного создания всех заказов
        cartItemRepository.deleteByUserId(userId);

        return createdOrders;
    }

    // Получить все заказы конкретного ресторана
    public List<Order> getOrdersByRestaurant(Long restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId);
    }

    // Получение всех заказов конкретного пользователя
    public List<Order> getUserOrders(Long userId) {
        // Убедись, что в OrderRepository есть метод findByUserId(Long userId)
        return orderRepository.findByUserId(userId); 
    }

    // Обновить статус заказа
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}
