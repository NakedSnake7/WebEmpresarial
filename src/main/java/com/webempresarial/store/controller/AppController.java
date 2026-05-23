package com.webempresarial.store.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.Cliente;
import com.webempresarial.store.service.UserService;
import com.webempresarial.store.theme.StoreResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AppController {

    private final UserService userService;
    private final StoreResolver storeResolver;

    public AppController(
            UserService userService,
            StoreResolver storeResolver
    ) {
        this.userService = userService;
        this.storeResolver = storeResolver;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(
            @Valid @RequestBody SubscriptionRequest request,
            BindingResult result,
            HttpServletRequest httpRequest
    ) {
        Store store = storeResolver.getCurrentStore(httpRequest);

        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(buildValidationErrorResponse(result));
        }

        if (userService.existsByEmail(request.getEmail(), store)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseMessage(
                            "El correo electrónico ya está registrado.",
                            null
                    ));
        }

        try {
            Cliente newUser = new Cliente(
                    request.getFullName(),
                    request.getEmail(),
                    null
            );

            userService.saveUser(newUser, store);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseMessage(
                            "Suscripción exitosa",
                            newUser
                    ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMessage(
                            "Ocurrió un error interno. Intente nuevamente más tarde.",
                            null
                    ));
        }
    }

    private ResponseMessage buildValidationErrorResponse(BindingResult result) {
        StringBuilder errorMessage = new StringBuilder();

        result.getAllErrors().forEach(error ->
                errorMessage
                        .append(error.getDefaultMessage())
                        .append(". ")
        );

        return new ResponseMessage(
                "Error en la validación",
                errorMessage.toString().trim()
        );
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubscriptionRequest {
        private String fullName;
        private String email;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class ResponseMessage {
        private String message;
        private Object data;

        public ResponseMessage(String message, Object data) {
            this.message = message;
            this.data = data;
        }

        public String getMessage() {
            return message;
        }

        public Object getData() {
            return data;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
}