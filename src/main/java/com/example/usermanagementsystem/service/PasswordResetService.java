package com.example.usermanagementsystem.service;

import com.example.usermanagementsystem.entity.PasswordResetToken;
import com.example.usermanagementsystem.entity.User;
import com.example.usermanagementsystem.repository.PasswordResetTokenRepository;
import com.example.usermanagementsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${app.password-reset.token.expiration}")
    private long tokenExpirationMs;
    
    @Value("${app.password-reset.email.base-url}")
    private String baseUrl;
    
    @Transactional
    public boolean createPasswordResetTokenForUser(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            logger.warn("Password reset requested for non-existent email: {}", email);
            return false;
        }
        
        User user = userOptional.get();
        
        // 删除该用户之前的密码重置令牌（如果存在）
        Optional<PasswordResetToken> existingToken = tokenRepository.findByUser(user);
        if (existingToken.isPresent()) {
            tokenRepository.delete(existingToken.get());
            tokenRepository.flush(); // 立即刷新以确保删除操作被提交
        }
        
        // 创建新的密码重置令牌
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(
                token,
                user,
                Instant.now().plusMillis(tokenExpirationMs)
        );
        
        tokenRepository.save(resetToken);
        logger.info("Created password reset token for user: {}", user.getUsername());
        
        // 发送密码重置邮件
        String resetUrl = baseUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), resetUrl, user.getUsername());
        
        return true;
    }
    
    @Transactional
    public boolean validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);
        
        if (tokenOptional.isEmpty()) {
            logger.warn("Invalid password reset token: {}", token);
            return false;
        }
        
        PasswordResetToken resetToken = tokenOptional.get();
        
        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            logger.warn("Expired password reset token: {}", token);
            return false;
        }
        
        return true;
    }
    
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);
        
        if (tokenOptional.isEmpty()) {
            logger.warn("Password reset attempted with invalid token: {}", token);
            return false;
        }
        
        PasswordResetToken resetToken = tokenOptional.get();
        
        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            logger.warn("Password reset attempted with expired token: {}", token);
            return false;
        }
        
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // 密码重置成功后删除令牌
        tokenRepository.delete(resetToken);
        logger.info("Password reset successful for user: {}", user.getUsername());
        
        return true;
    }
    
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteAllExpiredTokens(Instant.now());
        logger.info("Cleaned up expired password reset tokens");
    }
    
    /**
     * 根据令牌获取用户信息
     * @param token 密码重置令牌
     * @return 用户对象，如果令牌无效则返回null
     */
    public User getUserByToken(String token) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);
        
        if (tokenOptional.isEmpty()) {
            logger.warn("Attempted to get user with invalid token: {}", token);
            return null;
        }
        
        PasswordResetToken resetToken = tokenOptional.get();
        
        if (resetToken.isExpired()) {
            logger.warn("Attempted to get user with expired token: {}", token);
            return null;
        }
        
        return resetToken.getUser();
    }
}
