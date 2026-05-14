package com.fooddelivery.restaurant_app.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    // Секретный ключ для подписи токенов (в реальном проекте хранится в переменных окружения!)
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    // Токен будет жить 24 часа
    private final int jwtExpirationMs = 86400000;

    // Генерация токена на основе email пользователя
    // Добавили второй параметр - String role
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role) // <--- Вот она, магия! Кладем роль прямо в токен
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    // Достаем email из токена
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // Проверка: валидный ли токен и не истек ли он
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}