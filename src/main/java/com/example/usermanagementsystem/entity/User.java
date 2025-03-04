package com.example.usermanagementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "username"),
           @UniqueConstraint(columnNames = "email")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @Size(max = 120)
    @Column(nullable = true)
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
    
    // 新增非必填属性
    @Size(max = 20)
    @Pattern(regexp = "^$|^\\d{11}$", message = "手机号码必须为11位数字")
    private String phoneNumber;
    
    @Size(max = 50)
    private String realName;
    
    @Size(max = 200)
    private String address;
    
    @Size(max = 100)
    private String city;
    
    @Size(max = 20)
    private String postalCode;
    
    @Size(max = 50)
    private String country;
    
    @Size(max = 500)
    private String bio;
    
    // 用户是否启用
    private boolean enabled = true;
    
    // OAuth2相关字段
    @Column(name = "oauth_id", nullable = true)
    private String oauthId;
    
    @Column(name = "oauth_provider", nullable = true)
    private String oauthProvider;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
