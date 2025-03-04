package com.example.usermanagementsystem.security.jwt;

import com.example.usermanagementsystem.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        logger.debug("Filtering request: {}", path);
        
        try {
            String jwt = parseJwt(request);
            logger.debug("JWT token from request: {}", jwt != null ? "present" : "not present");
            
            if (jwt != null) {
                boolean isValid = jwtUtils.validateJwtToken(jwt);
                logger.debug("JWT token validation result: {}", isValid ? "valid" : "invalid");
                
                if (isValid) {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    logger.debug("Username from JWT token: {}", username);

                    try {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        logger.debug("User details loaded: {}", userDetails.getUsername());
                        
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.debug("Authentication set in SecurityContext");
                    } catch (Exception e) {
                        logger.error("Error loading user details for username {}: {}", username, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        // 首先尝试从Authorization头中获取
        String headerAuth = request.getHeader("Authorization");
        logger.debug("Authorization header: {}", headerAuth != null ? "present" : "not present");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            logger.debug("JWT token found in Authorization header");
            return headerAuth.substring(7);
        }
        
        // 如果Authorization头中没有，则尝试从Cookie中获取
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        logger.info("Cookies: {}", cookies != null ? cookies.length + " found" : "null");
        
        if (cookies != null) {
            logger.info("Checking cookies for JWT token");
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                // 打印完整的cookie值以便于调试
                logger.info("Cookie found: name={}, value={}, path={}, secure={}, httpOnly={}", 
                        cookie.getName(), 
                        cookie.getValue(), 
                        cookie.getPath(),
                        cookie.getSecure(),
                        cookie.isHttpOnly());
                
                if ("jwt".equals(cookie.getName())) {
                    logger.info("Found JWT token in cookie: {}", cookie.getValue());
                    return cookie.getValue();
                }
            }
            logger.info("No JWT token found in cookies");
        } else {
            logger.debug("No cookies found in request");
        }
        
        return null;
    }
}
