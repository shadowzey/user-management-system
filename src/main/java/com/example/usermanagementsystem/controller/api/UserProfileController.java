package com.example.usermanagementsystem.controller;

import com.example.usermanagementsystem.entity.User;
import com.example.usermanagementsystem.payload.request.UpdateProfileRequest;
import com.example.usermanagementsystem.payload.response.MessageResponse;
import com.example.usermanagementsystem.payload.response.UserProfileResponse;
import com.example.usermanagementsystem.security.services.UserDetailsImpl;
import com.example.usermanagementsystem.service.UserLogService;
import com.example.usermanagementsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
@Tag(name = "User Profile", description = "用户个人信息管理接口")
public class UserProfileController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserLogService userLogService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        summary = "获取用户个人信息", 
        description = "获取当前登录用户的个人信息",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userService.getUserById(userDetails.getId());
        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("用户不存在"));
        }
        
        return ResponseEntity.ok(new UserProfileResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getRealName(),
            user.getAddress(),
            user.getCity(),
            user.getPostalCode(),
            user.getCountry(),
            user.getBio(),
            user.isEnabled(),
            user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(java.util.stream.Collectors.toList())
        ));
    }
    
    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        summary = "更新用户个人信息", 
        description = "更新当前登录用户的个人信息",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> updateUserProfile(
            @Valid @RequestBody UpdateProfileRequest profileRequest,
            HttpServletRequest request) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            User updatedUser = userService.updateUserProfile(
                userDetails.getId(),
                profileRequest.getPhoneNumber(),
                profileRequest.getRealName(),
                profileRequest.getAddress(),
                profileRequest.getCity(),
                profileRequest.getPostalCode(),
                profileRequest.getCountry(),
                profileRequest.getBio()
            );
            
            // 记录个人信息更新日志
            userLogService.logProfileUpdate(
                updatedUser.getUsername(),
                updatedUser.getId(),
                request,
                true
            );
            
            return ResponseEntity.ok(new MessageResponse("个人信息更新成功"));
        } catch (Exception e) {
            // 记录个人信息更新失败日志
            userLogService.logProfileUpdate(
                userDetails.getUsername(),
                userDetails.getId(),
                request,
                false
            );
            
            return ResponseEntity.badRequest().body(new MessageResponse("个人信息更新失败: " + e.getMessage()));
        }
    }
}
