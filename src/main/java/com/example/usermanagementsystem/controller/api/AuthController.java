package com.example.usermanagementsystem.controller;

import com.example.usermanagementsystem.entity.ERole;
import com.example.usermanagementsystem.entity.Role;
import com.example.usermanagementsystem.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import com.example.usermanagementsystem.payload.request.LoginRequest;
import com.example.usermanagementsystem.payload.request.SignupRequest;
import com.example.usermanagementsystem.payload.response.JwtResponse;
import com.example.usermanagementsystem.payload.response.MessageResponse;
import com.example.usermanagementsystem.repository.RoleRepository;
import com.example.usermanagementsystem.repository.UserRepository;
import com.example.usermanagementsystem.security.jwt.JwtUtils;
import com.example.usermanagementsystem.security.services.UserDetailsImpl;
import com.example.usermanagementsystem.service.UserLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;
    
    @Autowired
    UserLogService userLogService;

    @PostMapping("/signin")
    @Operation(summary = "User login", description = "Authenticate a user and return a JWT token")
    public ResponseEntity<?> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();    
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            // 记录成功登录日志
            userLogService.logLogin(userDetails.getUsername(), userDetails.getId(), request, true);
            
            // 将JWT令牌添加到Cookie中
            Cookie jwtCookie = new Cookie("jwt", jwt);
            jwtCookie.setPath("/");
            jwtCookie.setHttpOnly(false); // 允许JavaScript访问
            jwtCookie.setMaxAge(24 * 60 * 60); // 24小时
            response.addCookie(jwtCookie);
            
            // 添加用户信息Cookie
            Cookie userCookie = new Cookie("user", "{\"id\":\""+userDetails.getId()+"\",\"username\":\""+userDetails.getUsername()+"\",\"roles\":[\""+String.join("\",\"", roles)+"\"]}");
            userCookie.setPath("/");
            userCookie.setHttpOnly(false);
            userCookie.setMaxAge(24 * 60 * 60);
            response.addCookie(userCookie);
            
            return ResponseEntity.ok(new JwtResponse(jwt, 
                                                     userDetails.getId(), 
                                                     userDetails.getUsername(), 
                                                     userDetails.getEmail(), 
                                                     roles));
        } catch (BadCredentialsException e) {
            // 记录失败登录日志
            userLogService.logLogin(loginRequest.getUsername(), null, request, false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("用户名或密码错误"));
        } catch (Exception e) {
            // 记录失败登录日志
            userLogService.logLogin(loginRequest.getUsername(), null, request, false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("登录失败: " + e.getMessage()));
        }
    }

    @PostMapping("/signup")
    @Operation(summary = "User registration", description = "Register a new user account")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody SignupRequest signUpRequest,
            HttpServletRequest request) {
        
        try {
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                userLogService.logRegister(signUpRequest.getUsername(), null, request, false);
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Username is already taken!"));
            }

            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                userLogService.logRegister(signUpRequest.getUsername(), null, request, false);
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Email is already in use!"));
            }

            // Create new user's account
            User user = new User(signUpRequest.getUsername(), 
                                signUpRequest.getEmail(),
                                encoder.encode(signUpRequest.getPassword()));

            // 无论用户在请求中指定了什么角色，都只允许注册为普通用户
            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);

            user.setRoles(roles);
            User savedUser = userRepository.save(user);
            
            // 记录成功注册日志
            userLogService.logRegister(savedUser.getUsername(), savedUser.getId(), request, true);

            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
            // 记录注册失败日志
            userLogService.logRegister(signUpRequest.getUsername(), null, request, false);
            throw e;
        }
    }
}
