package com.fooddelivery.restaurant_app.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fooddelivery.restaurant_app.models.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(length = 1000, nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    // Используем BigDecimal для денег, чтобы не было проблем с копейками
    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;
}
