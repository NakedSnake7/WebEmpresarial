package com.webempresarial.store.controller;

import com.webempresarial.store.dto.auth.RegisterRequestDTO;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.RegistrationService;
import com.webempresarial.store.theme.StoreResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final AuthenticationManager authenticationManager;
    private final StoreResolver storeResolver;

    public AuthController(
            RegistrationService registrationService,
            AuthenticationManager authenticationManager,
            StoreResolver storeResolver
    ) {
        this.registrationService = registrationService;
        this.authenticationManager = authenticationManager;
        this.storeResolver = storeResolver;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequestDTO dto,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);

        registrationService.register(dto, store);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        dto.getEmail(),
                        dto.getPassword()
                );

        Authentication authentication =
                authenticationManager.authenticate(authToken);

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        request.getSession(true)
                .setAttribute(
                        HttpSessionSecurityContextRepository
                                .SPRING_SECURITY_CONTEXT_KEY,
                        context
                );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Registro exitoso"
        ));
    }
}