package com.example.usermanagementsystem.config;

import com.example.usermanagementsystem.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    // 每天凌晨2点执行清理过期令牌的任务
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        passwordResetService.cleanupExpiredTokens();
    }
}
