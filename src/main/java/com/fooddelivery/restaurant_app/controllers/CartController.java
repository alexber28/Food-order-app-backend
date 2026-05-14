package com.fooddelivery.restaurant_app.controllers;

import com.fooddelivery.restaurant_app.models.CartItem;
import com.fooddelivery.restaurant_app.models.Dish;
import com.fooddelivery.restaurant_app.models.User;
import com.fooddelivery.restaurant_app.services.CartService;
import com.fooddelivery.restaurant_app.repositories.UserRepository;
import com.fooddelivery.restaurant_app.repositories.DishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;
    private final DishRepository dishRepository;

    public record CartRequest(Long dishId, int quantity) {}

    // Эндпоинт теперь: GET http://localhost:8080/api/cart (НИКАКИХ ID В ССЫЛКЕ!)
    @GetMapping
    public ResponseEntity<List<CartItem>> getUserCart(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(cartService.getUserCart(user.getId()));
    }

    // Эндпоинт теперь: POST http://localhost:8080/api/cart/add
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@AuthenticationPrincipal UserDetails userDetails, @RequestBody CartRequest request) {
        // Мы берем email прямо из токена (userDetails) и ищем юзера в базе
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        Dish dish = dishRepository.findById(request.dishId())
                .orElseThrow(() -> new RuntimeException("Блюдо не найдено"));

        cartService.addToCart(user, dish, request.quantity());
        return ResponseEntity.ok("Блюдо успешно добавлено в вашу корзину!");
    }

    // DELETE: Удалить конкретное блюдо из корзины юзера
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<String> removeFromCart(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long cartItemId) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Передаем ID текущего юзера в сервис для проверки
        cartService.removeCartItem(cartItemId, user.getId()); 
        return ResponseEntity.ok("Позиция удалена из корзины");
    }
}