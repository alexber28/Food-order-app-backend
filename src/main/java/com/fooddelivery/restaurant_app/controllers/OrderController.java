package com.fooddelivery.restaurant_app.controllers;

import com.fooddelivery.restaurant_app.models.Order;
import com.fooddelivery.restaurant_app.models.User;
import com.fooddelivery.restaurant_app.models.enums.OrderStatus;
import com.fooddelivery.restaurant_app.services.OrderService;
import com.fooddelivery.restaurant_app.repositories.OrderRepository;
import com.fooddelivery.restaurant_app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public record CreateOrderRequest(Long restaurantId, int waitTime) {}

    // Эндпоинт: POST http://localhost:8080/api/orders/create
    @PostMapping("/create")
    public ResponseEntity<List<Order>> createOrder(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        // Вызываем новый метод, который сам всё сделает и вернет список заказов
        List<Order> newOrders = orderService.createOrdersFromCart(user.getId());
        return ResponseEntity.ok(newOrders);
    }

    // GET: Посмотреть все заказы ресторана (Для менеджера и админа)
    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<Order>> getRestaurantOrders(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(orderService.getOrdersByRestaurant(restaurantId));
    }

    // Эндпоинт: GET http://localhost:8080/api/orders
    @GetMapping
    public ResponseEntity<List<Order>> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        // Находим юзера по токену
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        // Возвращаем его заказы
        List<Order> orders = orderService.getUserOrders(user.getId());
        return ResponseEntity.ok(orders);
    }

    // PUT: Изменить статус заказа (Для менеджера и админа)
    // Пример: PUT http://localhost:8080/api/orders/1/status?status=APPROVED
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PutMapping("/{id}/status-old")
    @Deprecated
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId, 
            @RequestParam OrderStatus status) {
        
        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(updatedOrder);
    }

    // Получить вообще все заказы (для админки)
    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    // Заготовка для смены статуса менеджером
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> updateOrderDetails(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        Order order = orderRepository.findById(id).orElseThrow();
        
        // Обновляем статус, если он есть в запросе
        if (body.containsKey("status")) {
            order.setStatus(com.fooddelivery.restaurant_app.models.enums.OrderStatus.valueOf(body.get("status")));
        }
        
        // Обновляем время ожидания, если оно есть в запросе
        if (body.containsKey("waitTime") && body.get("waitTime") != null) {
            order.setWaitTime(Integer.parseInt(body.get("waitTime")));
        }
        
        orderRepository.save(order);
        return ResponseEntity.ok("Данные заказа обновлены");
    }
    
}
