package com.example.usermanagementsystem.security.oauth2;

import com.example.usermanagementsystem.entity.ERole;
import com.example.usermanagementsystem.entity.Role;
import com.example.usermanagementsystem.entity.User;
import com.example.usermanagementsystem.repository.RoleRepository;
import com.example.usermanagementsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder encoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        // GitHub OAuth2 Response contains 'login' as the username attribute
        String username = (String) oAuth2User.getAttributes().get("login");
        String email = (String) oAuth2User.getAttributes().get("email");
        
        if (!StringUtils.hasText(email)) {
            email = username + "@github.com"; // 如果GitHub不返回邮箱，使用默认格式
        }

        Optional<User> userOptional = userRepository.findByUsername(username);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // 更新用户信息
            user.setOauthId((String) oAuth2User.getAttributes().get("id"));
            user.setOauthProvider("github");
            userRepository.save(user);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2User);
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                oAuth2User.getAttributes(),
                "login"
        );
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        User user = new User();
        
        // GitHub OAuth2 Response contains specific attributes
        user.setOauthProvider(oAuth2UserRequest.getClientRegistration().getRegistrationId());
        user.setOauthId((String) oAuth2User.getAttributes().get("id").toString());
        user.setUsername((String) oAuth2User.getAttributes().get("login"));
        user.setEmail((String) oAuth2User.getAttributes().get("email"));
        
        if (!StringUtils.hasText(user.getEmail())) {
            user.setEmail(user.getUsername() + "@github.com"); // 如果GitHub不返回邮箱，使用默认格式
        }
        
        // 为OAuth2用户设置随机密码，因为数据库不允许密码为null
        // 实际上这个密码不会被用户使用，因为他们会通过OAuth2登录
        String randomPassword = UUID.randomUUID().toString();
        user.setPassword(encoder.encode(randomPassword));
        
        user.setEnabled(true);
        
        // 设置用户角色
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
        user.setRoles(roles);
        
        return userRepository.save(user);
    }
}
