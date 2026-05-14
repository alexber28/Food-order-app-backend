package com.fooddelivery.restaurant_app.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "dishes")
@Data
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "section_name")
    private String sectionName; // Наш "Label" для группировки на фронтенде

    @ManyToOne // У одного ресторана может быть много блюд
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "is_visible")
    private Boolean visible = true;
}
