package com.webempresarial.store.commerce.web.storefront.checkout;

import com.stripe.model.checkout.Session;  
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.domain.order.Order.PaymentMethod;
import com.webempresarial.store.commerce.domain.order.OrderStatus;
import com.webempresarial.store.commerce.domain.order.PaymentStatus;
import com.webempresarial.store.commerce.infrastructure.checkout.payment.StripeCommerceCheckoutService;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.commerce.application.order.OrderService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.theme.StoreResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
public class StripeCheckoutController {

    private final OrderService orderService;
    private final StripeCommerceCheckoutService stripeCheckoutService;
    private final StoreResolver storeResolver;
    private final FeatureAccessService featureAccessService;

    public StripeCheckoutController(
            OrderService orderService,
            StripeCommerceCheckoutService stripeCheckoutService,
            StoreResolver storeResolver,
            FeatureAccessService featureAccessService
    ) {
        this.orderService = orderService;
        this.stripeCheckoutService = stripeCheckoutService;
        this.storeResolver = storeResolver;
        this.featureAccessService = featureAccessService;
    }
    
    private static final Logger logger =
            LoggerFactory.getLogger(StripeCheckoutController.class);
    
    @PostMapping("/create-session/{orderId}")
    @Transactional
    public ResponseEntity<?> createStripeSession(
            @PathVariable Long orderId,
            HttpServletRequest request
    ) {

        Store store = storeResolver.getCurrentStore(request);

        if (!featureAccessService.canUse(
                store,
                Feature.CHECKOUT
        )) {
            return ResponseEntity.notFound().build();
        }

        HttpSession httpSession =
                request.getSession(false);

        Object authorizedOrderId =
                httpSession != null
                        ? httpSession.getAttribute(
                                "CHECKOUT_CONFIRMATION_ORDER_ID"
                        )
                        : null;

        if (!(authorizedOrderId instanceof Long)
                || !orderId.equals(authorizedOrderId)) {

            return ResponseEntity.notFound().build();
        }

        Order order =
                orderService.getByIdForStripeForUpdate(
                        orderId,
                        store
                );
        if (order.getPaymentMethod()
                != PaymentMethod.STRIPE) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    "La orden no corresponde a un pago con Stripe"
                            )
                    );
        }

        if (!store.isStripePaymentAvailable()) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    "La tienda debe conectar Stripe "
                                            + "antes de aceptar pagos con tarjeta"
                            )
                    );
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La orden ya fue pagada"));
        }

        if (order.getPaymentStatus() != PaymentStatus.PENDING
                || order.getOrderStatus() != OrderStatus.CREATED) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    "La orden ya no está disponible para pago"
                            )
                    );
        }

        try {

            if (order.getStripeSessionId() != null) {

                boolean expired =
                        stripeCheckoutService.isSessionExpired(
                                order.getStripeSessionId(),
                                store
                        );

                if (!expired) {
                    return ResponseEntity.ok(
                            Map.of(
                                    "url",
                                    stripeCheckoutService.getSessionUrl(
                                            order.getStripeSessionId(),
                                            store
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