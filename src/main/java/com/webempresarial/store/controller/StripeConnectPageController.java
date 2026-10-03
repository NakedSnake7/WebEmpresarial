package com.webempresarial.store.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StripeConnectService;

@Controller
public class StripeConnectPageController {

    private final StripeConnectService stripeConnectService;
    private final StoreContextService storeContextService;

    public StripeConnectPageController(
            StripeConnectService stripeConnectService,
            StoreContextService storeContextService
    ) {
        this.stripeConnectService =
                stripeConnectService;

        this.storeContextService =
                storeContextService;
    }

    @GetMapping("/admin/stripe/connect/return")
    public RedirectView connectReturn(
            HttpServletRequest request
    ) {

        Store store =
                storeContextService
                        .getCurrentStore(request);

        boolean connected =
                stripeConnectService
                        .syncConnectionStatus(store);

        if (connected) {
            return new RedirectView(
                    "/admin/store/settings"
                            + "?stripeConnected"
            );
        }

        return new RedirectView(
                "/admin/store/settings"
                        + "?stripePending"
        );
    }

    @GetMapping("/admin/stripe/connect/refresh")
    public RedirectView connectRefresh(
            HttpServletRequest request
    ) {

        /*
         * Stripe Account Links son de un solo uso.
         * No intentamos reutilizar el anterior.
         *
         * Regresamos a settings para que el usuario
         * inicie nuevamente el onboarding.
         */
        return new RedirectView(
                "/admin/store/settings"
                        + "?stripeRefresh"
        );
    }
}