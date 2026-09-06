package com.webempresarial.store.commerce.web.storefront.order;

import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.commerce.application.order.OrderService;
import com.webempresarial.store.theme.StoreResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pedidos")
public class PedidoClienteController {

    private final OrderService orderService;
    private final StoreResolver storeResolver;

    public PedidoClienteController(
            OrderService orderService,
            StoreResolver storeResolver
    ) {
        this.orderService = orderService;
        this.storeResolver = storeResolver;
    }

    @GetMapping("/{id}")
    public String verPedidoCliente(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);

        Order order = orderService.getOrderByIdWithUserAndItems(id, store);

        if (!order.getCustomerEmail().equalsIgnoreCase(userDetails.getUsername())) {
            return "error/403";
        }

        model.addAttribute("pedido", order);
        return "pedido-detalle";
    }
}