package com.webempresarial.store.config;

import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.repository.AdminUserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder
    ) {

        return args -> {

            String adminEmail = "admin@admin.com";

            boolean exists =
                    adminUserRepository.existsByEmail(adminEmail);

            if (!exists) {

                AdminUser admin = new AdminUser();

                admin.setFullName("WebEmpresarial");

                admin.setEmail(adminEmail);

                admin.setPassword(
                        passwordEncoder.encode("Admin123*")
                );

                admin.setRole(AdminRole.SUPER_ADMIN);

                admin.setEnabled(true);

                adminUserRepository.save(admin);

                System.out.println(
                        "🔥 SUPER ADMIN creado: " + adminEmail
                );

            } else {

                System.out.println(
                        "✅ SUPER ADMIN ya existe"
                );
            }
        };
    }
}