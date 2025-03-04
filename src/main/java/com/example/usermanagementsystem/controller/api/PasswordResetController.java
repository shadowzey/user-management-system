package com.example.usermanagementsystem.controller;

import com.example.usermanagementsystem.entity.User;
import com.example.usermanagementsystem.payload.request.ForgotPasswordRequest;
import com.example.usermanagementsystem.payload.request.ResetPasswordRequest;
import com.example.usermanagementsystem.payload.response.MessageResponse;
import com.example.usermanagementsystem.service.PasswordResetService;
import com.example.usermanagementsystem.service.UserLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
@Tag(name = "Password Reset", description = "密码重置相关接口")
public class PasswordResetController {
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    @Autowired
    private UserLogService userLogService;
    
    @PostMapping("/forgot")
    @Operation(summary = "忘记密码", description = "发送密码重置邮件")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest servletRequest) {
        
        boolean result = passwordResetService.createPasswordResetTokenForUser(request.getEmail());
        
        // 记录密码重置请求日志
        userLogService.logPasswordResetRequest(request.getEmail(), null, servletRequest, result);
        
        // 无论邮箱是否存在，都返回成功，避免泄露用户信息
        return ResponseEntity.ok(new MessageResponse("如果邮箱存在，密码重置邮件已发送"));
    }
    
    @GetMapping("/validate-token")
    @Operation(summary = "验证令牌", description = "验证密码重置令牌是否有效")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        boolean isValid = passwordResetService.validatePasswordResetToken(token);
        
        if (isValid) {
            return ResponseEntity.ok(new MessageResponse("令牌有效"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("令牌无效或已过期"));
        }
    }
    
    @PostMapping("/reset")
    @Operation(summary = "重置密码", description = "使用令牌重置密码")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest servletRequest) {
        
        // 获取用户信息以便记录日志
        User user = passwordResetService.getUserByToken(request.getToken());
        String username = user != null ? user.getUsername() : "unknown";
        Long userId = user != null ? user.getId() : null;
        
        boolean result = passwordResetService.resetPassword(request.getToken(), request.getPassword());
        
        // 记录密码重置日志
        userLogService.logPasswordReset(username, userId, servletRequest, result);
        
        if (result) {
            return ResponseEntity.ok(new MessageResponse("密码重置成功"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("密码重置失败，令牌无效或已过期"));
        }
    }
}
