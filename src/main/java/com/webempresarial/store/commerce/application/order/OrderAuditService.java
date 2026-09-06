package com.webempresarial.store.commerce.application.order;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderAuditLog;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.domain.order.OrderAuditAction;
import com.webempresarial.store.commerce.domain.order.OrderStatus;
import com.webempresarial.store.commerce.domain.order.PaymentStatus;
import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderAuditLogRepository;

@Service
public class OrderAuditService {

    private final OrderAuditLogRepository repository;

    public OrderAuditService(
            OrderAuditLogRepository repository
    ) {
        this.repository = repository;
    }

    public List<OrderAuditLog> findTimeline(
            Long orderId,
            Long storeId
    ) {
        if (orderId == null || storeId == null) {
            throw new IllegalArgumentException(
                    "orderId y storeId son obligatorios"
            );
        }

        return repository
                .findByOrderIdAndStoreIdOrderByCreatedAtAsc(
                        orderId,
                        storeId
                );
    }
    
    public void record(
            Order order,
            OrderAuditAction action,
            OrderStatus previousOrderStatus,
            PaymentStatus previousPaymentStatus,
            String reason
    ) {
        if (order == null
                || order.getId() == null
                || order.getStore() == null
                || order.getStore().getId() == null) {
            throw new IllegalArgumentException(
                    "La orden persistida y su tienda son obligatorias"
            );
        }

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String actorUsername = "SYSTEM";
        String actorType = "SYSTEM";

        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(
                        authentication.getPrincipal()
                )) {

            actorUsername = authentication.getName();

            actorType = authentication
                    .getAuthorities()
                    .stream()
                    .map(authority -> authority.getAuthority())
                    .findFirst()
                    .orElse("AUTHENTICATED");
        }

        OrderAuditLog audit = new OrderAuditLog();

        audit.setOrderId(order.getId());
        audit.setStoreId(order.getStore().getId());
        audit.setAction(action);

        audit.setPreviousOrderStatus(
                previousOrderStatus
        );

        audit.setNewOrderStatus(
                order.getOrderStatus()
        );

        audit.setPreviousPaymentStatus(
                previousPaymentStatus
        );

        audit.setNewPaymentStatus(
                order.getPaymentStatus()
        );

        audit.setActorUsername(actorUsername);
        audit.setActorType(actorType);
        audit.setReason(reason);

        repository.save(audit);
    }
}