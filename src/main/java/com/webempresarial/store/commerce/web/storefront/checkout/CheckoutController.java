package com.webempresarial.store.commerce.web.storefront.checkout;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import com.webempresarial.store.dto.checkout.CheckoutRequestDTO;
import com.webempresarial.store.model.Cliente;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.commerce.application.checkout.CheckoutService;
import com.webempresarial.store.service.UserService;
import com.webempresarial.store.theme.StoreResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class CheckoutController {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    CheckoutController.class
            );

    private final CheckoutService checkoutService;
    private final UserService userService;
    private final StoreResolver storeResolver;

    public CheckoutController(
            CheckoutService checkoutService,
            UserService userService,
            StoreResolver storeResolver
    ) {
        this.checkoutService = checkoutService;
        this.userService = userService;
        this.storeResolver = storeResolver;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> processCheckout(
            @Valid @RequestBody CheckoutRequestDTO checkoutRequest,
            HttpServletRequest request
    ) {

        Store store =
                storeResolver.getCurrentStore(request);

        try {

            Order order = checkoutService.createOrder(
                    checkoutRequest,
                    store
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "orderId", order.getId(),
                            "message",
                            "¡Orden creada correctamente!"
                    ));

        } catch (IllegalArgumentException ex) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", ex.getMessage()
                    ));

        } catch (Exception ex) {

            logger.error(
                    "Error en checkout de tienda {}",
                    store != null ? store.getId() : null,
                    ex
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message",
                            "Error al procesar la orden"
                    ));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(
            MethodArgumentNotValidException ex
    ) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return ResponseEntity.badRequest().body(
                Map.of(
                        "success", false,
                        "errors", errors
                )
        );
    }

    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
    ) {
        if (userDetails == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        Store store =
                storeResolver.getCurrentStore(request);

        return userService
                .findByEmail(
                        userDetails.getUsername(),
                        store
                )
                .<ResponseEntity<?>>map(cliente ->
                        ResponseEntity.ok(
                                Map.of(
                                        "email",
                                        safe(cliente.getEmail()),

                                        "fullName",
                                        safe(cliente.getFullName()),

                                        "phone",
                                        safe(cliente.getPhone()),

                                        "address",
                                        safe(cliente.getDefaultAddress())
                                )
                        )
                )
                .orElseGet(() ->
                        ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .build()
                );
    }

    private String safe(String value) {
        return value != null ? value : "";
    }
}