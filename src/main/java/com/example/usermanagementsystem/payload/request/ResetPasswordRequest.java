package com.example.usermanagementsystem.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    
    @NotBlank(message = "令牌不能为空")
    private String token;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 40, message = "密码长度必须在6到40个字符之间")
    private String password;
}
