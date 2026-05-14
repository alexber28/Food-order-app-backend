package com.fooddelivery.restaurant_app.models.enums;

public enum OrderStatus {
    CREATED,   // Создан (ожидает подтверждения)
    APPROVED,  // Одобрен менеджером (готовится)
    READY,     // Готов к выдаче
    DELIVERING,
    COMPLETED,
    CANCELLED  // Отменен
}
