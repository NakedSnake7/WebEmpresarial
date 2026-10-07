package com.webempresarial.store.config;

import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.repository.AdminUserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    @ConditionalOnProperty(
            prefix = "app.bootstrap.super-admin",
            name = "enabled",
            havingValue = "true"
    )
    CommandLineRunner initAdmin(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.super-admin.email:}") String configuredEmail,
            @Value("${app.bootstrap.super-admin.password:}") String configuredPassword,
            @Value("${app.bootstrap.super-admin.full-name:WebEmpresarial}") String configuredFullName
    ) {

        return args -> {

            String adminEmail =
                    configuredEmail != null
                            ? configuredEmail.trim()
                            : "";

            String adminPassword =
                    configuredPassword != null
                            ? configuredPassword
                            : "";

            String adminFullName =
                    configuredFullName != null
                            ? configuredFullName.trim()
                            : "";

            if (adminEmail.isBlank()) {
                throw new IllegalStateException(
                        "SUPER_ADMIN_EMAIL es obligatorio cuando "
                                + "SUPER_ADMIN_BOOTSTRAP_ENABLED=true"
                );
            }

            if (adminPassword.isBlank()) {
                throw new IllegalStateException(
                        "SUPER_ADMIN_PASSWORD es obligatorio cuando "
                                + "SUPER_ADMIN_BOOTSTRAP_ENABLED=true"
                );
            }

            if (adminPassword.length() < 12) {
                throw new IllegalStateException(
                        "SUPER_ADMIN_PASSWORD debe tener al menos 12 caracteres"
                );
            }

            if (adminFullName.isBlank()) {
                adminFullName = "WebEmpresarial";
            }

            if (adminUserRepository.existsByEmail(adminEmail)) {
                System.out.println(
                        "✅ SUPER ADMIN ya existe: " + adminEmail
                );
                return;
            }

            AdminUser admin = new AdminUser();

            admin.setFullName(adminFullName);
            admin.setEmail(adminEmail);
            admin.setPassword(
                    passwordEncoder.encode(adminPassword)
            );
            admin.setRole(AdminRole.SUPER_ADMIN);
            admin.setEnabled(true);

            adminUserRepository.save(admin);

            System.out.println(
                    "🔥 SUPER ADMIN creado: " + adminEmail
            );
        };
    }
}
