package com.webempresarial.store.commerce.web.storefront.order;

import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.commerce.application.order.OrderService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.theme.StoreResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pedidos")
public class PedidoClienteController {

    private final OrderService orderService;
    private final StoreResolver storeResolver;
    private final FeatureAccessService featureAccessService;

    public PedidoClienteController(
            OrderService orderService,
            StoreResolver storeResolver,
            FeatureAccessService featureAccessService
    ) {
        this.orderService = orderService;
        this.storeResolver = storeResolver;
        this.featureAccessService = featureAccessService;
    }

    @GetMapping("/{id}")
    public String verPedidoCliente(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Store store = storeResolver.getCurrentStore(request);

        if (!featureAccessService.canUse(
                store,
                Feature.ORDERS
        )) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND
            );
        }

        Order order =
                orderService.getOrderByIdWithUserAndItems(
                        id,
                        store
                );

        if (!order.getCustomerEmail()
                .equalsIgnoreCase(
                        userDetails.getUsername()
                )) {

            response.setStatus(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return "error/403";
        }

        model.addAttribute("pedido", order);
        return "pedido-detalle";
    }
}