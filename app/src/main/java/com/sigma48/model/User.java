package com.sigma48.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "role", // Gunakan field "role" di JSON untuk menentukan tipe Java Class
    visible = true    // Pastikan field "role" tetap bisa di-deserialize ke objek
)
@JsonSubTypes({
    // Jika nilai "role" di JSON adalah "AGEN_LAPANGAN", buat instance dari kelas Agent.java
    @JsonSubTypes.Type(value = Agent.class, name = "AGEN_LAPANGAN"),

    // Untuk semua peran lain, tetap buat instance dari kelas User.java
    @JsonSubTypes.Type(value = User.class, name = "DIREKTUR_INTELIJEN"),
    @JsonSubTypes.Type(value = User.class, name = "ANALIS_INTELIJEN"),
    @JsonSubTypes.Type(value = User.class, name = "KOMANDAN_OPERASI"),
    @JsonSubTypes.Type(value = User.class, name = "ADMIN")
})

public class User {
    private String id;
    private String username;
    private String passwordHash;
    private Role role;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    public User() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.isActive = true;    
    }

    public User(String username, String passwordHash, Role role) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @JsonProperty("isActive")
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
}