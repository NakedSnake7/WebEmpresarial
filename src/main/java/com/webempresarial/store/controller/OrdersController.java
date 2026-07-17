package com.webempresarial.store.controller;

import com.webempresarial.store.model.Order;
import com.webempresarial.store.model.OrderStatus;
import com.webempresarial.store.model.PaymentStatus;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.OrderAuditLogRepository;
import com.webempresarial.store.service.OrderService;
import com.webempresarial.store.theme.StoreResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/orders")
public class OrdersController {

    private final OrderService orderService;
    private final StoreResolver storeResolver;
    private final OrderAuditLogRepository orderAuditLogRepository;

    public OrdersController(
            OrderService orderService,
            StoreResolver storeResolver,
            OrderAuditLogRepository orderAuditLogRepository
    ) {
        this.orderService = orderService;
        this.storeResolver = storeResolver;
        this.orderAuditLogRepository =
                orderAuditLogRepository;
    }

    @GetMapping
    public String listarOrders(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentStatus payment,
            @RequestParam(required = false) String search,
            Model model,
            HttpServletRequest request
    ) {

        Store store = storeResolver.getCurrentStore(request);

        List<Order> orders = orderService.filterOrders(
                from,
                to,
                status,
                payment,
                store
        );

        if (search != null && !search.isBlank()) {
            String text = search.toLowerCase();

            orders = orders.stream()
                    .filter(o ->
                            o.getCustomerName() != null &&
                            o.getCustomerName().toLowerCase().contains(text)
                    )
                    .toList();
        }

        orders = orders.stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .toList();

        model.addAttribute("orders", orders);
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("paymentStatuses", PaymentStatus.values());

        return "admin/orders";
    }

    @GetMapping("/{id}")
    public String verDetalles(
            @PathVariable Long id,
            Model model,
            HttpServletRequest request
    ) {

        Store store = storeResolver.getCurrentStore(request);

        Order order = orderService.getOrderByIdWithUserAndItems(
                id,
                store
        );

        model.addAttribute("order", order);
        model.addAttribute(
                "auditLogs",
                orderAuditLogRepository
                        .findByOrderIdAndStoreIdOrderByCreatedAtAsc(
                                id,
                                store.getId()
                        )
        );

        return "admin/order-details";
    }

    @PostMapping("/{id}/confirm-payment")
    public String confirmarPagoTransferencia(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {

        Store store = storeResolver.getCurrentStore(request);

        try {
            orderService.confirmarPagoTransferencia(id, store);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Pago confirmado correctamente. Orden aprobada."
            );

        } catch (IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/orders/" + id;
    }

    @PostMapping("/update-shipping")
    public String updateShipping(
            @RequestParam Long orderId,
            @RequestParam String courier,
            @RequestParam String trackingNumber,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {

        Store store = storeResolver.getCurrentStore(request);

        try {
            orderService.updateShippingInfo(
                    orderId,
                    trackingNumber,
                    courier,
                    store
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Envío actualizado"
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Error al actualizar envío"
            );
        }

        return "redirect:/orders/" + orderId;
    }

    @PostMapping("/{id}/cancel")
    public String cancelarOrden(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);

        try {
            orderService.cancelOrder(id, store);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Orden cancelada correctamente. El inventario fue restaurado."
            );

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible cancelar la orden."
            );
        }

        return "redirect:/orders";
    }

    @PostMapping("/{id}/status-ajax")
    @ResponseBody
    public Map<String, String> updateOrderStatusAjax(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            HttpServletRequest request
    ) {

        Store store = storeResolver.getCurrentStore(request);

        Order order = orderService.updateOrderStatus(
                id,
                payload.get("status"),
                store
        );

        Map<String, String> response = new HashMap<>();

        response.put("label", order.getOrderStatusLabel());
        response.put("badge", order.getOrderStatusBadge());

        return response;
    }
}