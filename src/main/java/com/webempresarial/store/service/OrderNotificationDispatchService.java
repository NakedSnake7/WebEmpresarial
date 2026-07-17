package com.webempresarial.store.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import com.webempresarial.store.events.order.OrderNotificationRequestedEvent;
import com.webempresarial.store.exceptions.OrderNotFoundException;
import com.webempresarial.store.model.Order;
import com.webempresarial.store.model.OrderStatus;
import com.webempresarial.store.model.PaymentStatus;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.OrderRepository;
import com.webempresarial.store.repository.StoreRepository;

@Service
public class OrderNotificationDispatchService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    OrderNotificationDispatchService.class
            );

    private final EmailService emailService;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;

    public OrderNotificationDispatchService(
            EmailService emailService,
            OrderRepository orderRepository,
            StoreRepository storeRepository
    ) {
        this.emailService = emailService;
        this.orderRepository = orderRepository;
        this.storeRepository = storeRepository;
    }
    
    private boolean hasValidCustomerEmail(Order order) {
        return order.getCustomerEmail() != null
                && !order.getCustomerEmail().isBlank();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatch(
            OrderNotificationRequestedEvent event
    ) {
        Store store = storeRepository
                .findById(event.storeId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Tienda no encontrada: "
                                        + event.storeId()
                        )
                );

        Order order = orderRepository
                .findByIdFullAndStore(
                        event.orderId(),
                        store
                )
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Orden no encontrada: "
                                        + event.orderId()
                        )
                );

        try {
            switch (event.type()) {
                case TRANSFER_INSTRUCTIONS ->
                        sendTransferInstructions(order);

                case PAYMENT_CONFIRMATION ->
                        sendPaymentConfirmation(order);

                case SHIPPING_CONFIRMATION ->
                        sendShipping(order);

                case ORDER_EXPIRED ->
                        sendExpired(
                                order,
                                event.expirationDate()
                        );
            }

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Error enviando notificación "
                            + event.type()
                            + " para orderId="
                            + event.orderId(),
                    ex
            );
        }
    }

    private void sendTransferInstructions(
            Order order
    ) throws IOException {

        if (order.getPaymentMethod()
                != Order.PaymentMethod.TRANSFER) {
            return;
        }

        if (order.isTransferInstructionsSent()) {
            return;
        }
        
        if (!hasValidCustomerEmail(order)) {
            log.warn(
                    "[Order Notification] Orden {} sin correo de cliente",
                    order.getId()
            );
            return;
        }

        emailService.enviarCorreoDatosTransferencia(order);
        order.setTransferInstructionsSent(true);

        log.info(
                "[Order Notification] Instrucciones enviadas orderId={}",
                order.getId()
        );
    }

    private void sendPaymentConfirmation(
            Order order
    ) throws IOException {

        if (order.getPaymentStatus() != PaymentStatus.PAID
                || order.getOrderStatus() != OrderStatus.PROCESSED
                || order.isPaymentConfirmedSent()) {
            return;
        }
        
        if (!hasValidCustomerEmail(order)) {
            log.warn(
                    "[Order Notification] Orden {} sin correo de cliente",
                    order.getId()
            );
            return;
        }

        emailService.enviarCorreoPedidoProcesado(
                order.getCustomerEmail(),
                order.getCustomerName(),
                order.getId(),
                order.getItems()
        );

        order.setPaymentConfirmedSent(true);

        log.info(
                "[Order Notification] Confirmación enviada orderId={}",
                order.getId()
        );
    }

    private void sendShipping(
            Order order
    ) throws IOException {

        if (order.getPaymentStatus() != PaymentStatus.PAID
                || order.getOrderStatus() != OrderStatus.SHIPPED
                || order.isShippingConfirmationSent()
                || order.getTrackingNumber() == null
                || order.getTrackingNumber().isBlank()) {
            return;
        }

        if (!hasValidCustomerEmail(order)) {
            log.warn(
                    "[Order Notification] Orden {} sin correo de cliente",
                    order.getId()
            );
            return;
        }

        emailService.enviarCorreoEnvio(
                order.getCustomerEmail(),
                order.getCustomerName(),
                order.getId(),
                order.getOrderDate().toString(),
                order.getTrackingNumber(),
                order.getCarrier()
        );

        order.setShippingConfirmationSent(true);

        log.info(
                "[Order Notification] Envío confirmado orderId={}",
                order.getId()
        );
    }

    private void sendExpired(
            Order order,
            java.time.LocalDateTime expirationDate
    ) throws IOException {

        if (order.getPaymentMethod()
                    != Order.PaymentMethod.TRANSFER
                || order.getPaymentStatus()
                    != PaymentStatus.EXPIRED
                || order.getOrderStatus()
                    != OrderStatus.CANCELLED
                || order.isOrderExpiredSent()) {
            return;
        }
        
        if (!hasValidCustomerEmail(order)) {
            log.warn(
                    "[Order Notification] Orden {} sin correo de cliente",
                    order.getId()
            );
            return;
        }

        java.time.LocalDateTime effectiveExpirationDate =
                expirationDate != null
                        ? expirationDate
                        : order.getOrderDate().plusHours(24);

        emailService.enviarCorreoOrdenExpirada(
                order,
                effectiveExpirationDate
        );

        order.setOrderExpiredSent(true);

        log.info(
                "[Order Notification] Expiración enviada orderId={}",
                order.getId()
        );
    }
}