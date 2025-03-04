package com.example.usermanagementsystem.service;

import com.example.usermanagementsystem.entity.UserLog;
import com.example.usermanagementsystem.repository.UserLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class UserLogService {
    
    @Autowired
    private UserLogRepository userLogRepository;
    
    // 常量定义
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_REGISTER = "REGISTER";
    public static final String ACTION_PASSWORD_RESET_REQUEST = "PASSWORD_RESET_REQUEST";
    public static final String ACTION_PASSWORD_RESET = "PASSWORD_RESET";
    public static final String ACTION_PASSWORD_CHANGE = "PASSWORD_CHANGE";
    public static final String ACTION_PROFILE_UPDATE = "PROFILE_UPDATE";
    
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    
    /**
     * 创建用户日志
     */
    public UserLog createLog(String action, String username, Long userId, String ipAddress, String details, String status) {
        UserLog log = new UserLog(action, username, userId, ipAddress, details, status);
        return userLogRepository.save(log);
    }
    
    /**
     * 从HttpServletRequest中获取客户端IP地址
     */
    public String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }
    
    /**
     * 记录登录操作
     */
    public UserLog logLogin(String username, Long userId, HttpServletRequest request, boolean success) {
        String status = success ? STATUS_SUCCESS : STATUS_FAILED;
        String details = success ? "User login successful" : "User login failed";
        return createLog(ACTION_LOGIN, username, userId, getClientIp(request), details, status);
    }
    
    /**
     * 记录注册操作
     */
    public UserLog logRegister(String username, Long userId, HttpServletRequest request, boolean success) {
        String status = success ? STATUS_SUCCESS : STATUS_FAILED;
        String details = success ? "User registration successful" : "User registration failed";
        return createLog(ACTION_REGISTER, username, userId, getClientIp(request), details, status);
    }
    
    /**
     * 记录密码重置请求
     */
    public UserLog logPasswordResetRequest(String username, Long userId, HttpServletRequest request, boolean success) {
        String status = success ? STATUS_SUCCESS : STATUS_FAILED;
        String details = success ? "Password reset request successful" : "Password reset request failed";
        return createLog(ACTION_PASSWORD_RESET_REQUEST, username, userId, getClientIp(request), details, status);
    }
    
    /**
     * 记录密码重置操作
     */
    public UserLog logPasswordReset(String username, Long userId, HttpServletRequest request, boolean success) {
        String status = success ? STATUS_SUCCESS : STATUS_FAILED;
        String details = success ? "Password reset successful" : "Password reset failed";
        return createLog(ACTION_PASSWORD_RESET, username, userId, getClientIp(request), details, status);
    }
    
    /**
     * 记录密码修改操作
     */
    public UserLog logPasswordChange(String username, Long userId, HttpServletRequest request, boolean success) {
        String status = success ? STATUS_SUCCESS : STATUS_FAILED;
        String details = success ? "Password change successful" : "Password change failed";
        return createLog(ACTION_PASSWORD_CHANGE, username, userId, getClientIp(request), details, status);
    }
    
    /**
     * 记录用户资料更新操作
     */
    public UserLog logProfileUpdate(String username, Long userId, HttpServletRequest request, boolean success) {
        String status = success ? STATUS_SUCCESS : STATUS_FAILED;
        String details = success ? "Profile update successful" : "Profile update failed";
        return createLog(ACTION_PROFILE_UPDATE, username, userId, getClientIp(request), details, status);
    }
    
    /**
     * 记录登出操作
     */
    public UserLog logLogout(String username, Long userId, HttpServletRequest request) {
        return createLog(ACTION_LOGOUT, username, userId, getClientIp(request), "User logout successful", STATUS_SUCCESS);
    }
    
    /**
     * 获取用户的所有日志
     */
    public List<UserLog> getUserLogs(String username) {
        return userLogRepository.findByUsername(username);
    }
    
    /**
     * 获取用户的所有日志（通过用户ID）
     */
    public List<UserLog> getUserLogsById(Long userId) {
        return userLogRepository.findByUserId(userId);
    }
    
    /**
     * 获取特定操作的所有日志
     */
    public List<UserLog> getLogsByAction(String action) {
        return userLogRepository.findByAction(action);
    }
    
    /**
     * 获取特定时间范围内的日志
     */
    public List<UserLog> getLogsBetween(Instant start, Instant end) {
        return userLogRepository.findByTimestampBetween(start, end);
    }
    
    /**
     * 获取特定状态的日志
     */
    public List<UserLog> getLogsByStatus(String status) {
        return userLogRepository.findByStatus(status);
    }
}
