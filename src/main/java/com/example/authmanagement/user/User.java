package com.example.authmanagement.user;

import com.example.authmanagement.enums.Role;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = Role.class)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    String accessToken;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime accessTokenExpirationDate;

    String refreshToken;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime refreshTokenExpirationDate;
}
