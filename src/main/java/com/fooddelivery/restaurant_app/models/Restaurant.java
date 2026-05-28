package com.fooddelivery.restaurant_app.models;

import jakarta.persistence.*;
import lombok.Data;
//import java.util.List;

@Entity
@Table(name = "restaurants")
@Data
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    // Генерируем геттер и сеттер для imageUrl
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Column(name = "is_visible")
    private Boolean visible = true;

    // Связь: у ресторана один менеджер (из таблицы users)
    @OneToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }
}