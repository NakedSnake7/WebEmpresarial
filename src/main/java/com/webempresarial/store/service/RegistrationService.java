package com.webempresarial.store.service;

import com.webempresarial.store.dto.auth.RegisterRequestDTO;
import com.webempresarial.store.model.AuthUser;
import com.webempresarial.store.model.AuthUser.Role;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.Cliente;
import com.webempresarial.store.repository.AuthUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final AuthUserRepository authUserRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            AuthUserRepository authUserRepository,
            UserService userService,
            PasswordEncoder passwordEncoder
    ) {
        this.authUserRepository = authUserRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequestDTO dto, Store store) {

        String email = dto.getEmail().trim().toLowerCase();

        if (authUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El usuario ya está registrado");
        }

        Cliente cliente = userService.registerUser(
                email,
                dto.getFullName().trim(),
                dto.getPhone(),
                store
        );

        AuthUser authUser = new AuthUser();
        authUser.setEmail(email);
        authUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        authUser.setRole(Role.CLIENTE);
        authUser.setEnabled(true);

        authUserRepository.save(authUser);
    }
}