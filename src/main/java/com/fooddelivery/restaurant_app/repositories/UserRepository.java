package com.fooddelivery.restaurant_app.repositories;

import com.fooddelivery.restaurant_app.models.User;
import com.fooddelivery.restaurant_app.models.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email); // Для проверки при регистрации

    // Вот этот метод: Spring сам поймет, как сделать SQL запрос по полю role
    List<User> findByRole(Role role);
}
