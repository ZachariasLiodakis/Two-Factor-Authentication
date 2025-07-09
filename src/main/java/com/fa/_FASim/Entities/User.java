package com.fa._FASim.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;  // Primary key, auto-generated

    @NotBlank
    @Size(max = 20)
    private String username;  // Username, must not be blank, max length 20

    @NotBlank
    @Size(max = 120)
    private String password;  // Encrypted password, must not be blank, max length 120

    @NotBlank
    @Size(max = 50)
    private String email;  // User email, must not be blank, max length 50

    // Flag to indicate if Two-Factor Authentication is enabled for this user
    private boolean twoFactorEnabled = false;

    // This field is not persisted in the database; used for temporary logic (e.g. "remember me" checkbox)
    @Transient
    private boolean rememberMe;

    // Secret key for TOTP 2FA, stored in DB with max length 64 characters
    @Column(length = 64)
    private String secret;

    // Default no-arg constructor required by JPA
    public User() {
    }

    // Constructor with all fields including optional 2FA settings
    public User(String username, String password, String email, boolean twoFactorEnabled, String secret) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.twoFactorEnabled = twoFactorEnabled;
        this.secret = secret;
    }

    // Getters and setters for all fields below:

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    // toString method excludes sensitive info, hides secret for security reasons
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", twoFactorEnabled=" + twoFactorEnabled +
                ", secret='" + (secret != null ? "[PROTECTED]" : null) + '\'' +
                '}';
    }
}
