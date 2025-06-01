package com.sigma48.model;

import java.util.UUID;

public class User {
    private String id;
    private String username;
    private String passwordHash;
    private Role role;

    public User() {
        this.id = UUID.randomUUID().toString();
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
}