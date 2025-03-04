package com.example.usermanagementsystem.security;

import com.example.usermanagementsystem.security.jwt.AuthEntryPointJwt;
import com.example.usermanagementsystem.security.jwt.AuthTokenFilter;
import com.example.usermanagementsystem.security.oauth2.CustomOAuth2UserService;
import com.example.usermanagementsystem.security.oauth2.OAuth2AuthenticationSuccessHandler;
import com.example.usermanagementsystem.security.services.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);
    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;
    
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;
    
    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
    
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 将PasswordEncoder定义为静态Bean，避免循环依赖
     */
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(java.util.Arrays.asList("http://localhost:8080"));
        configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> 
                auth.requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/password/**").permitAll()
                    .requestMatchers("/api/user/**").authenticated()
                    .requestMatchers("/").permitAll()
                    .requestMatchers("/reset-password").permitAll()
                    .requestMatchers("/forgot-password").permitAll()
                    .requestMatchers("/login").permitAll()
                    .requestMatchers("/signup").permitAll()
                    .requestMatchers("/profile").permitAll()
                    .requestMatchers("/admin/**").permitAll()
                    .requestMatchers("/css/**", "/js/**", "/images/**", "webfonts/**", "/favicon.ico").permitAll()
                    .requestMatchers("/api-docs/**").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/swagger-ui.html").permitAll()
                    .requestMatchers("/login/oauth2/code/**").permitAll() // OAuth2回调URL
                    .requestMatchers("/oauth-callback.html").permitAll() // OAuth2回调页面
                    .anyRequest().authenticated()
            );
        
        // 配置OAuth2登录
        logger.info("Configuring OAuth2 login");
        logger.info("CustomOAuth2UserService: {}", (customOAuth2UserService != null ? "available" : "not available"));
        logger.info("OAuth2AuthenticationSuccessHandler: {}", (oAuth2AuthenticationSuccessHandler != null ? "available" : "not available"));
        
        http.oauth2Login(oauth2 -> {
            oauth2.userInfoEndpoint(userInfo -> 
                userInfo.userService(customOAuth2UserService)
            );
            oauth2.successHandler(oAuth2AuthenticationSuccessHandler);
            // 不设置defaultSuccessUrl，让我们的successHandler完全控制重定向
            // oauth2.defaultSuccessUrl("/profile");
            // 添加调试信息
            logger.info("OAuth2 login configured with success handler: {}", oAuth2AuthenticationSuccessHandler.getClass().getName());
        });
        
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
