package com.webempresarial.store.controller;

import com.stripe.model.checkout.Session; 
import com.webempresarial.store.model.Order;
import com.webempresarial.store.model.PaymentStatus;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.OrderService;
import com.webempresarial.store.service.StripeCheckoutService;
import com.webempresarial.store.theme.StoreResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
public class StripeCheckoutController {

    private final OrderService orderService;
    private final StripeCheckoutService stripeCheckoutService;
    private final StoreResolver storeResolver;

    public StripeCheckoutController(
            OrderService orderService,
            StripeCheckoutService stripeCheckoutService,
            StoreResolver storeResolver
    ) {
        this.orderService = orderService;
        this.stripeCheckoutService = stripeCheckoutService;
        this.storeResolver = storeResolver;
    }
    
    private static final Logger logger =
            LoggerFactory.getLogger(StripeCheckoutController.class);
    
    @PostMapping("/create-session/{orderId}")
    public ResponseEntity<?> createStripeSession(
            @PathVariable Long orderId,
            HttpServletRequest request
    ) {

        Store store = storeResolver.getCurrentStore(request);

        Order order = orderService.getById(orderId, store);

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La orden ya fue pagada"));
        }

        try {

            if (order.getStripeSessionId() != null) {

                boolean expired =
                        stripeCheckoutService.isSessionExpired(
                                order.getStripeSessionId()
                        );

                if (!expired) {
                    return ResponseEntity.ok(
                            Map.of(
                                    "url",
                                    stripeCheckoutService.getSessionUrl(
                                            order.getStripeSessionId()
                                    )
                            )
                    );
                }

                order.setStripeSessionId(null);
                orderService.save(order, store);
            }

            Session session =
                    stripeCheckoutService.createSession(order);

            order.setStripeSessionId(session.getId());

            orderService.save(order, store);

            return ResponseEntity.ok(
                    Map.of("url", session.getUrl())
            );

        } catch (Exception e) {

        	logger.error("Error al crear sesión Stripe para orderId={}", orderId, e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error al crear sesión de pago"));
        }
    }
}