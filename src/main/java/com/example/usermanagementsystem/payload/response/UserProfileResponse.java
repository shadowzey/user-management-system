package com.example.usermanagementsystem.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String realName;
    private String address;
    private String city;
    private String postalCode;
    private String country;
    private String bio;
    private boolean enabled;
    private List<String> roles;
    
    // 兼容旧版本构造函数
    public UserProfileResponse(Long id, String username, String email, String phoneNumber, String realName, 
                              String address, String city, String postalCode, String country, String bio) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.realName = realName;
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
        this.bio = bio;
        this.enabled = true;
        this.roles = null;
    }
}
