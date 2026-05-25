package com.webempresarial.store.service;

import java.time.LocalDateTime; 

import org.springframework.stereotype.Service;

import java.io.IOException;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.model.Order;
import com.webempresarial.store.model.OrderStatus;
import com.webempresarial.store.model.PaymentStatus;
import com.webempresarial.store.repository.OrderRepository;

@Service
public class NotificationService {

    private final EmailService emailService;
    private final OrderRepository orderRepository;

    public NotificationService(EmailService emailService,
                               OrderRepository orderRepository) {
        this.emailService = emailService;
        this.orderRepository = orderRepository;
    }

    // ============================
    // TRANSFERENCIA
    // ============================
    @Transactional
    public void sendTransferInstructions(Order order) {

        if (order.getPaymentMethod() != Order.PaymentMethod.TRANSFER) return;
        if (order.isTransferInstructionsSent()) return;

        try {
            emailService.enviarCorreoDatosTransferencia(order);

            order.setTransferInstructionsSent(true);
            orderRepository.save(order);

        } catch (IOException e) {
            System.err.println("⚠️ Error correo transferencia. Orden=" + order.getId());
        }
    }

    // ============================
    // CONFIRMACIÓN DE PAGO
    // ============================
    @Transactional
    public void sendPaymentConfirmation(Order order) {

        if (order.getPaymentStatus() != PaymentStatus.PAID) return;
        if (order.getOrderStatus() != OrderStatus.PROCESSED) return;
        if (order.isPaymentConfirmedSent()) return;

        try {
            emailService.enviarCorreoPedidoProcesado(
                    order.getCustomerEmail(),
                    order.getCustomerName(),
                    order.getId(),
                    order.getItems()
            );

            order.setPaymentConfirmedSent(true);
            orderRepository.save(order);

        } catch (IOException e) {
            System.err.println("⚠️ Error correo confirmación. Orden=" + order.getId());
        }
    }

    // ============================
    // ENVÍO
    // ============================
    @Transactional
    public void sendShipping(Order order) {

        if (order.getPaymentStatus() != PaymentStatus.PAID) return;
        if (order.getOrderStatus() != OrderStatus.SHIPPED) return;
        if (order.isShippingConfirmationSent()) return;
        if (order.getTrackingNumber() == null || order.getTrackingNumber().isBlank()) return;

        try {
            emailService.enviarCorreoEnvio(
                    order.getCustomerEmail(),
                    order.getCustomerName(),
                    order.getId(),
                    order.getOrderDate().toString(),
                    order.getTrackingNumber(),
                    order.getCarrier()
            );

            order.setShippingConfirmationSent(true);
            orderRepository.save(order);

        } catch (IOException e) {
            System.err.println("⚠️ Error correo envío. Orden=" + order.getId());
        }
    }

    // ============================
    // ORDEN EXPIRADA
    // ============================
    @Transactional
    public void sendExpired(Order order, LocalDateTime limite) {

        if (order.getPaymentMethod() != Order.PaymentMethod.TRANSFER) return;
        if (order.getPaymentStatus() != PaymentStatus.EXPIRED) return;
        if (order.getOrderStatus() != OrderStatus.CANCELLED) return;
        if (order.isOrderExpiredSent()) return;

        try {
            emailService.enviarCorreoOrdenExpirada(order, limite);

            order.setOrderExpiredSent(true);
            orderRepository.save(order);

        } catch (IOException e) {
            System.err.println("⚠️ Error correo expiración. Orden=" + order.getId());
        }
    }
    
    public void notifyNewLead(Lead lead) {
        System.out.println("Nuevo lead recibido: " + lead.getNombre());
    }
    
}