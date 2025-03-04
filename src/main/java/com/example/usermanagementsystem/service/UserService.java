package com.example.usermanagementsystem.service;

import com.example.usermanagementsystem.entity.User;
import com.example.usermanagementsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 根据用户ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户对象，如果不存在则返回null
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
    
    /**
     * 更新用户个人信息
     * 
     * @param userId 用户ID
     * @param phoneNumber 手机号码
     * @param realName 真实姓名
     * @param address 地址
     * @param city 城市
     * @param postalCode 邮政编码
     * @param country 国家
     * @param bio 个人简介
     * @return 更新后的用户对象
     */
    @Transactional
    public User updateUserProfile(
            Long userId,
            String phoneNumber,
            String realName,
            String address,
            String city,
            String postalCode,
            String country,
            String bio) {
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setPhoneNumber(phoneNumber);
        user.setRealName(realName);
        user.setAddress(address);
        user.setCity(city);
        user.setPostalCode(postalCode);
        user.setCountry(country);
        user.setBio(bio);
        
        logger.info("更新用户 {} 的个人信息", user.getUsername());
        return userRepository.save(user);
    }
    
    /**
     * 获取所有用户（分页）
     * 
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    /**
     * 获取所有用户（不分页）
     * 
     * @return 用户列表
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * 通过用户名查找用户
     * 
     * @param username 用户名
     * @return 用户对象（可能为空）
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * 更新用户状态（启用/禁用）
     * 
     * @param userId 用户ID
     * @param enabled 是否启用
     * @return 更新后的用户对象
     */
    @Transactional
    public User updateUserStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setEnabled(enabled);
        
        String action = enabled ? "启用" : "禁用";
        logger.info("{} 用户 {}", action, user.getUsername());
        return userRepository.save(user);
    }
    
    /**
     * 管理员更新用户信息
     * 
     * @param userId 用户ID
     * @param phoneNumber 手机号码
     * @param realName 真实姓名
     * @param address 地址
     * @param city 城市
     * @param postalCode 邮政编码
     * @param country 国家
     * @param bio 个人简介
     * @param enabled 是否启用
     * @return 更新后的用户对象
     */
    @Transactional
    public User adminUpdateUser(
            Long userId,
            String phoneNumber,
            String realName,
            String address,
            String city,
            String postalCode,
            String country,
            String bio,
            boolean enabled) {
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setPhoneNumber(phoneNumber);
        user.setRealName(realName);
        user.setAddress(address);
        user.setCity(city);
        user.setPostalCode(postalCode);
        user.setCountry(country);
        user.setBio(bio);
        user.setEnabled(enabled);
        
        logger.info("管理员更新用户 {} 的信息", user.getUsername());
        return userRepository.save(user);
    }
    
    /**
     * 删除用户
     * 
     * @param userId 用户ID
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        logger.info("删除用户 {}", user.getUsername());
        userRepository.deleteById(userId);
    }
}
