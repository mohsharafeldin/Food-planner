package com.example.foodplanner.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Entity(tableName = "users", indices = { @Index(value = "email", unique = true) })
public class User {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String email;
    private String passwordHash;

    public User() {
        this.id = UUID.randomUUID().toString();
    }

    public User(@NonNull String name, @NonNull String email, @NonNull String password) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.passwordHash = hashPassword(password);
    }

    // Hash password using SHA-256
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to plain text (not recommended for production)
            return password;
        }
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
