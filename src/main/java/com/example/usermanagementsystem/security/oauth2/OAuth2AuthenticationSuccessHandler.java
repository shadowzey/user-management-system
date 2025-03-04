package com.example.usermanagementsystem.security.oauth2;

import com.example.usermanagementsystem.entity.ERole;
import com.example.usermanagementsystem.entity.Role;
import com.example.usermanagementsystem.entity.User;
import com.example.usermanagementsystem.repository.RoleRepository;
import com.example.usermanagementsystem.repository.UserRepository;
import com.example.usermanagementsystem.security.jwt.JwtUtils;
import com.example.usermanagementsystem.security.services.UserDetailsImpl;
import com.example.usermanagementsystem.security.services.UserDetailsServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                        Authentication authentication) throws IOException, ServletException {
        logger.info("OAuth2AuthenticationSuccessHandler.onAuthenticationSuccess called");
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        logger.debug("OAuth2User attributes: {}", oAuth2User.getAttributes());
        String username = (String) oAuth2User.getAttributes().get("login");
        logger.info("Extracted username: {}", username);
        
        // 加载用户详情
        try {
            // 尝试加载用户详情
            UserDetailsImpl userDetails;
            try {
                userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
                logger.info("User details loaded successfully: {}", userDetails.getUsername());
            } catch (UsernameNotFoundException e) {
                // 如果用户不存在，创建新用户
                logger.info("User not found, creating new user: {}", username);
                User newUser = createNewUser(oAuth2User);
                userDetails = UserDetailsImpl.build(newUser);
                logger.info("New user created: {}", newUser.getUsername());
            }
            
            // 生成JWT令牌
            String jwt = jwtUtils.generateJwtToken(userDetails);
            logger.debug("Generated JWT token: {}", jwt);
        
        // 将JWT令牌添加到Cookie
        Cookie jwtCookie = new Cookie("jwt", jwt);
        jwtCookie.setPath("/");
        // 设置HttpOnly为false，允许JavaScript访问
        jwtCookie.setHttpOnly(false);
        // 设置过期时间
        jwtCookie.setMaxAge(24 * 60 * 60); // 24小时
        
        // 打印调试信息
        logger.debug("Setting JWT cookie: {}", jwt);
        logger.debug("Cookie path: {}", jwtCookie.getPath());
        logger.debug("Cookie max age: {}", jwtCookie.getMaxAge());
        
        response.addCookie(jwtCookie);
        
        // 直接在响应中设置Cookie头
        String cookieValue = "jwt=" + jwt + "; Path=/; Max-Age=" + (24 * 60 * 60);
        response.addHeader("Set-Cookie", cookieValue);
        
        // 在响应头中设置token
        response.setHeader("Authorization", "Bearer " + jwt);
        
        // 重定向到前端页面
        String targetUrl = determineTargetUrl(request, response, authentication);
        
        // 添加JWT令牌到URL
        targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", jwt)
                .build().toUriString();
        
        // 记录日志
        logger.info("OAuth2登录成功，用户名: {}", username);
        logger.info("重定向URL: {}", targetUrl);
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } catch (UsernameNotFoundException e) {
            logger.error("Error loading user details: {}", e.getMessage());
            // 如果用户不存在，重定向到登录页面
            getRedirectStrategy().sendRedirect(request, response, "/login?error=oauth2_user_not_found");
        } catch (Exception e) {
            logger.error("Unexpected error in OAuth2AuthenticationSuccessHandler: {}", e.getMessage(), e);
            // 发生其他异常，重定向到登录页面
            getRedirectStrategy().sendRedirect(request, response, "/login?error=oauth2_error");
        }
    }
    
    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, 
                                        Authentication authentication) {
        logger.debug("determineTargetUrl called");
        // 使用Spring Security默认的OAuth2回调URL
        return "/login/oauth2/code/github";
    }
    
    /**
     * 创建新用户
     * @param oAuth2User OAuth2用户信息
     * @return 新创建的用户
     */
    private User createNewUser(OAuth2User oAuth2User) {
        User user = new User();
        
        // 设置基本信息
        String username = (String) oAuth2User.getAttributes().get("login");
        String email = (String) oAuth2User.getAttributes().get("email");
        if (email == null || email.isEmpty()) {
            email = username + "@github.com";
        }
        
        user.setUsername(username);
        user.setEmail(email);
        
        // 设置OAuth2相关信息
        user.setOauthId(oAuth2User.getAttributes().get("id").toString());
        user.setOauthProvider("github");
        
        // 设置随机密码
        String randomPassword = UUID.randomUUID().toString();
        user.setPassword(passwordEncoder.encode(randomPassword));
        
        // 设置用户角色
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
        user.setRoles(roles);
        
        // 设置其他信息
        user.setEnabled(true);
        if (oAuth2User.getAttributes().get("name") != null) {
            user.setRealName((String) oAuth2User.getAttributes().get("name"));
        }
        if (oAuth2User.getAttributes().get("bio") != null) {
            user.setBio((String) oAuth2User.getAttributes().get("bio"));
        }
        if (oAuth2User.getAttributes().get("location") != null) {
            user.setAddress((String) oAuth2User.getAttributes().get("location"));
        }
        
        return userRepository.save(user);
    }
}
