package com.example.usermanagementsystem.controller.api;

import com.example.usermanagementsystem.entity.User;
import com.example.usermanagementsystem.repository.UserRepository;
import com.example.usermanagementsystem.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/password")
public class PasswordManagementController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @PostMapping("/change")
    public ResponseEntity<?> changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword) {
        
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // 检查用户是否是OAuth2用户
        Optional<User> userOptional = userRepository.findByUsername(userDetails.getUsername());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // 如果是OAuth2用户，禁止修改密码
            if (user.getOauthProvider() != null && !user.getOauthProvider().isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "OAuth2用户不能修改密码");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证当前密码
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "当前密码不正确");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 更新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "密码修改成功");
            return ResponseEntity.ok(response);
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "用户不存在");
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/reset-request")
    public ResponseEntity<?> requestPasswordReset(@RequestParam("email") String email) {
        // 检查用户是否是OAuth2用户
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // 如果是OAuth2用户，禁止重置密码
            if (user.getOauthProvider() != null && !user.getOauthProvider().isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "OAuth2用户不能重置密码，请使用" + user.getOauthProvider() + "登录");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 正常处理密码重置逻辑
            // ...
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "密码重置链接已发送到您的邮箱");
            return ResponseEntity.ok(response);
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "用户不存在");
        return ResponseEntity.badRequest().body(response);
    }
}
