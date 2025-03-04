package com.example.usermanagementsystem.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebController.class);
    
    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "reset-password";
    }
    
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }
    
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }
    
    @GetMapping("/profile")
    public String profilePage() {
        return "profile";
    }
    
    @GetMapping("/login/oauth2/code/github")
    public String oauth2GithubCallback() {
        // 此方法只是为了确保路由存在，实际处理由Spring Security的OAuth2处理器完成
        logger.info("WebController.oauth2GithubCallback called");
        return "oauth-callback";
    }
    
    @GetMapping("/oauth-callback.html")
    public String oauthCallbackPage() {
        return "oauth-callback";
    }
    
    @GetMapping("/")
    public String homePage() {
        return "index";
    }
    
    @GetMapping("/admin")
    // 临时移除安全注解，以便调试
    // @PreAuthorize("hasRole('ADMIN')")
    public String adminPage() {
        return "admin/dashboard";
    }
    
    @GetMapping("/admin/users")
    // 临时移除安全注解，以便调试
    // @PreAuthorize("hasRole('ADMIN')")
    public String adminUsersPage() {
        return "admin/users";
    }
    
    @GetMapping("/admin/users/edit/{id}")
    // 临时移除安全注解，以便调试
    // @PreAuthorize("hasRole('ADMIN')")
    public String adminEditUserPage(@PathVariable Long id) {
        return "admin/edit-user";
    }
    
    @GetMapping("/admin/logs")
    // 临时移除安全注解，以便调试
    // @PreAuthorize("hasRole('ADMIN')")
    public String adminLogsPage() {
        return "admin/logs";
    }
}
