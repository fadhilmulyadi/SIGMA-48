package com.sigma48.model;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "role",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Agent.class, name = "AGEN_LAPANGAN"),
    @JsonSubTypes.Type(value = User.class, name = "DIREKTUR_INTELIJEN"),
    @JsonSubTypes.Type(value = User.class, name = "ANALIS_INTELIJEN"),
    @JsonSubTypes.Type(value = User.class, name = "KOMANDAN_OPERASI"),
    @JsonSubTypes.Type(value = User.class, name = "ADMIN")
})
public class User {

    private String id = UUID.randomUUID().toString();
    private String username;
    private String passwordHash;
    private Role role;

    @JsonProperty("isActive")
    private boolean isActive = true;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastLogin;

    public User(String username, String passwordHash, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }
}
