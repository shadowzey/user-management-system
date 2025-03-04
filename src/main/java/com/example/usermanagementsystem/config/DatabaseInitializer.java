package com.example.usermanagementsystem.config;

import com.example.usermanagementsystem.entity.ERole;
import com.example.usermanagementsystem.entity.Role;
import com.example.usermanagementsystem.entity.User;
import com.example.usermanagementsystem.repository.RoleRepository;
import com.example.usermanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Initialize roles if they don't exist
        initRoles();
        
        // Initialize admin user if not exists
        createAdminUserIfNotExists();
    }
    
    private void initRoles() {
        for (ERole role : ERole.values()) {
            if (roleRepository.findByName(role).isEmpty()) {
                Role savedRole = roleRepository.save(new Role(role));
                System.out.println("Role created: " + role.name() + " with ID: " + savedRole.getId());
            } else {
                System.out.println("Role already exists: " + role.name());
            }
        }
    }
    
    @Autowired
    private org.springframework.core.env.Environment env;
    
    private void createAdminUserIfNotExists() {
        String adminUsername = env.getProperty("app.admin.username", "admin");
        String adminEmail = env.getProperty("app.admin.email", "admin@example.com");
        String adminPassword = env.getProperty("app.admin.password", "admin123");
        
        System.out.println("Checking if admin user exists: " + adminUsername);
        
        if (!userRepository.existsByUsername(adminUsername)) {
            System.out.println("Admin user does not exist. Creating new admin user...");
            
            // Create admin user
            User adminUser = new User();
            adminUser.setUsername(adminUsername);
            adminUser.setEmail(adminEmail);
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            
            // Assign admin role
            Set<Role> roles = new HashSet<>();
            roleRepository.findByName(ERole.ROLE_ADMIN)
                    .ifPresentOrElse(
                        role -> {
                            roles.add(role);
                            System.out.println("Added ROLE_ADMIN to admin user");
                        },
                        () -> System.out.println("ROLE_ADMIN not found in database!")
                    );
            adminUser.setRoles(roles);
            
            User savedUser = userRepository.save(adminUser);
            System.out.println("Default admin user created with ID: " + savedUser.getId() + ", username: " + adminUsername);
        } else {
            System.out.println("Admin user already exists: " + adminUsername);
        }
    }
}
