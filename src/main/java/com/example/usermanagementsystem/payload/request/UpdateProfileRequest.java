package com.example.usermanagementsystem.payload.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    
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
}
