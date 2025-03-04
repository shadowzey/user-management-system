package com.example.usermanagementsystem.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_logs")
public class UserLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String action;
    
    @Column(nullable = false)
    private String username;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(nullable = false)
    private String ipAddress;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    @Column(nullable = false)
    private Instant timestamp;
    
    @Column(name = "status")
    private String status; // SUCCESS, FAILED
    
    // 默认构造函数
    public UserLog() {
        this.timestamp = Instant.now();
    }
    
    // 带参数的构造函数
    public UserLog(String action, String username, Long userId, String ipAddress, String details, String status) {
        this.action = action;
        this.username = username;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.details = details;
        this.status = status;
        this.timestamp = Instant.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
