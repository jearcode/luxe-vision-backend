package com.luxevision.backend.Security;

import com.luxevision.backend.entity.User;
import com.luxevision.backend.entity.UserRole;
import com.luxevision.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class InitialDataLoader {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Bean
    public ApplicationRunner run() {
        return args -> {
            if (!userRepository.existsByEmail("admin@admin.com")) {

                String passSinCifrar = "admin";
                String passCifrado = passwordEncoder.encode(passSinCifrar);
                User adminUser = new User();
                adminUser.setFirstName("admin");
                adminUser.setLastName("admin");
                adminUser.setPassword(passCifrado);
                adminUser.setEmail("admin@admin.com");
                adminUser.setUserRole(UserRole.ROLE_ADMIN);
                adminUser.setSignupDate(LocalDateTime.now());
                userRepository.save(adminUser);


                System.out.println("Usuarios de prueba creados con roles asignados.");
            }
        };
    }
}