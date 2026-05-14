package com.fooddelivery.restaurant_app.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "order_items")
@Data
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;

    @Column(name = "price_at_purchase", nullable = false)
    private BigDecimal priceAtPurchase;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @ManyToOne
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;
}
