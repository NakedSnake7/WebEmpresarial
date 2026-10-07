package com.webempresarial.store.service;

import com.webempresarial.store.entity.AdminAccountActivationToken;
import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.model.SubscriptionStatus;
import com.webempresarial.store.repository.AdminAccountActivationTokenRepository;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.repository.StoreSettingsRepository;
import com.webempresarial.store.repository.SubscriptionRepository;

import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class SaaSTenantLifecycleIntegrationTest {

    @Autowired
    private ProvisioningService provisioningService;

    @Autowired
    private AdminAccountActivationService activationService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreSettingsRepository storeSettingsRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private AdminAccountActivationTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private com.webempresarial.store.repository.ProductoRepository
            productoRepository;

    @Autowired
    private com.webempresarial.store.commerce.infrastructure.order.persistence.OrderRepository
            orderRepository;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private com.webempresarial.store.commerce.infrastructure.order.notification.NotificationService
            notificationService;

    @Test
    void shouldProvisionAndActivateNewSaaSTenant() {

        String domain =
                "e2e-lifecycle";

        String finalDomain =
                domain + ".web-empresarial.com";

        String email =
                "owner-e2e-lifecycle@webempresarial.test";

        Store provisionedStore =
                provisioningService.provisionStoreFromCheckout(
                        "E2E Lifecycle Store",
                        domain,
                        "E2E Owner",
                        email,
                        StorePlan.PRO,
                        "cus_e2e_lifecycle",
                        "sub_e2e_lifecycle",
                        "price_e2e_pro"
                );

        assertThat(provisionedStore.getId())
                .isNotNull();

        Store persistedStore =
                storeRepository
                        .findByDominio(finalDomain)
                        .orElseThrow();

        assertThat(persistedStore.getId())
                .isEqualTo(provisionedStore.getId());

        assertThat(persistedStore.isActiva())
                .isTrue();

        assertThat(persistedStore.getPlan())
                .isEqualTo(StorePlan.PRO);

        assertThat(
                storeSettingsRepository.findByStoreId(
                        persistedStore.getId()
                )
        ).isPresent();

        Subscription subscription =
                subscriptionRepository
                        .findByStoreId(
                                persistedStore.getId()
                        )
                        .orElseThrow();

        assertThat(subscription.getStatus())
                .isEqualTo(
                        SubscriptionStatus.ACTIVE
                );

        assertThat(subscription.getPlan())
                .isEqualTo(StorePlan.PRO);

        assertThat(
                subscription.getStripeCustomerId()
        ).isEqualTo(
                "cus_e2e_lifecycle"
        );

        assertThat(
                subscription.getStripeSubscriptionId()
        ).isEqualTo(
                "sub_e2e_lifecycle"
        );

        assertThat(
                subscription.getStripePriceId()
        ).isEqualTo(
                "price_e2e_pro"
        );

        AdminUser admin =
                adminUserRepository
                        .findByEmail(email)
                        .orElseThrow();

        assertThat(admin.getRole())
                .isEqualTo(
                        AdminRole.STORE_ADMIN
                );

        assertThat(admin.getStore().getId())
                .isEqualTo(
                        persistedStore.getId()
                );

        assertThat(admin.isEnabled())
                .isFalse();

        AdminAccountActivationToken token =
                tokenRepository
                        .findFirstByAdminUserIdAndUsedFalseOrderByCreatedAtDesc(
                                admin.getId()
                        )
                        .orElseThrow();

        assertThat(token.getToken())
                .isNotBlank();

        assertThat(token.isUsed())
                .isFalse();

        String newPassword =
                "StrongE2EPassword123!";

        activationService.activate(
                token.getToken(),
                newPassword
        );

        AdminUser activatedAdmin =
                adminUserRepository
                        .findByEmail(email)
                        .orElseThrow();

        assertThat(activatedAdmin.isEnabled())
                .isTrue();

        assertThat(
                passwordEncoder.matches(
                        newPassword,
                        activatedAdmin.getPassword()
                )
        ).isTrue();

        AdminAccountActivationToken usedToken =
                tokenRepository
                        .findByToken(
                                token.getToken()
                        )
                        .orElseThrow();

        assertThat(usedToken.isUsed())
                .isTrue();
    }
    @Test
    void activatedTenantAdminShouldLoginAndAccessOwnSettings()
            throws Exception {

        String domain =
                "e2e-admin-login";

        String finalDomain =
                domain + ".web-empresarial.com";

        String email =
                "owner-e2e-admin-login@webempresarial.test";

        provisioningService.provisionStoreFromCheckout(
                "E2E Admin Login Store",
                domain,
                "E2E Admin Owner",
                email,
                StorePlan.PRO,
                "cus_e2e_admin_login",
                "sub_e2e_admin_login",
                "price_e2e_admin_login"
        );

        AdminUser admin =
                adminUserRepository
                        .findByEmail(email)
                        .orElseThrow();

        AdminAccountActivationToken token =
                tokenRepository
                        .findFirstByAdminUserIdAndUsedFalseOrderByCreatedAtDesc(
                                admin.getId()
                        )
                        .orElseThrow();

        String password =
                "StrongAdminLogin123!";

        activationService.activate(
                token.getToken(),
                password
        );

        /*
         * Simula la frontera real entre:
         *
         * webhook/provisioning -> request posterior de login/admin.
         *
         * El provisioning y esta prueba comparten una transacción de test;
         * sin limpiar el persistence context, Store puede conservar en
         * memoria el lado inverso subscription == null aunque la relación
         * ya esté persistida correctamente.
         */
        entityManager.flush();
        entityManager.clear();

        MvcResult loginResult =
                mockMvc.perform(
                        post("/admin/login")
                                .with(csrf())
                                .param(
                                        "username",
                                        email
                                )
                                .param(
                                        "password",
                                        password
                                )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andReturn();

        String redirect =
                loginResult
                        .getResponse()
                        .getRedirectedUrl();

        assertThat(redirect)
                .isNotNull()
                .contains(finalDomain)
                .endsWith("/admin/dashboard");

        MockHttpSession session =
                (MockHttpSession)
                        loginResult
                                .getRequest()
                                .getSession(false);

        assertThat(session)
                .isNotNull();

        mockMvc.perform(
                get("/admin/store/settings")
                        .session(session)
                        .header(
                                "X-Forwarded-Host",
                                finalDomain
                        )
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                view().name(
                        "admin/store/settings"
                )
        );

        /*
         * El propietario personaliza su tenant utilizando
         * la misma sesión autenticada.
         */
        mockMvc.perform(
                post("/admin/store/settings")
                        .session(session)
                        .header(
                                "X-Forwarded-Host",
                                finalDomain
                        )
                        .with(csrf())
                        .param(
                                "companyEmail",
                                "ventas-e2e@webempresarial.test"
                        )
                        .param(
                                "contactName",
                                "E2E Admin Owner"
                        )
                        .param(
                                "currency",
                                "MXN"
                        )
                        .param(
                                "primaryColor",
                                "#123456"
                        )
                        .param(
                                "secondaryColor",
                                "#654321"
                        )
                        .param(
                                "accentColor",
                                "#ABCDEF"
                        )
                        .param(
                                "fontFamily",
                                "Inter"
                        )
                        .param(
                                "slogan",
                                "E2E-STOREFRONT-SLOGAN"
                        )
                        .param(
                                "heroTitle",
                                "E2E-STOREFRONT-HERO"
                        )
                        .param(
                                "heroSubtitle",
                                "E2E-STOREFRONT-SUBTITLE"
                        )
                        .param(
                                "footerText",
                                "E2E-STOREFRONT-FOOTER"
                        )
        )
        .andExpect(
                status().is3xxRedirection()
        );

        /*
         * Nueva frontera de request/contexto:
         * comprobamos lo realmente persistido.
         */
        entityManager.flush();
        entityManager.clear();

        var persistedSettings =
                storeSettingsRepository
                        .findByStoreId(
                                provisionedStoreId(finalDomain)
                        )
                        .orElseThrow();

        assertThat(persistedSettings.getPrimaryColor())
                .isEqualTo("#123456");

        assertThat(persistedSettings.getSlogan())
                .isEqualTo(
                        "E2E-STOREFRONT-SLOGAN"
                );

        assertThat(persistedSettings.getHeroTitle())
                .isEqualTo(
                        "E2E-STOREFRONT-HERO"
                );

        /*
         * Finalmente se solicita el storefront real del tenant.
         */
        MvcResult storefrontResult =
                mockMvc.perform(
                        get("/")
                                .header(
                                        "X-Forwarded-Host",
                                        finalDomain
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andReturn();

        String storefrontHtml =
                storefrontResult
                        .getResponse()
                        .getContentAsString();

        assertThat(storefrontHtml)
                .contains(
                        "E2E-STOREFRONT-SLOGAN",
                        "E2E-STOREFRONT-HERO",
                        "#123456"
                );

        /*
         * ==================================================
         * COMMERCE SMOKE FLOW
         * ==================================================
         *
         * admin -> producto -> storefront -> checkout
         * -> inventario -> orden -> administración
         */

        String productName =
                "E2E Commerce Product";

        mockMvc.perform(
                post("/nuevo")
                        .session(session)
                        .header(
                                "X-Forwarded-Host",
                                finalDomain
                        )
                        .with(csrf())
                        .param(
                                "productName",
                                productName
                        )
                        .param(
                                "description",
                                "Producto creado por el smoke test E2E"
                        )
                        .param(
                                "precio",
                                "250.00"
                        )
                        .param(
                                "stockSimple",
                                "10"
                        )
                        .param(
                                "visibleEnMenu",
                                "true"
                        )
                        .param(
                                "nuevaCategoria",
                                "E2E Category"
                        )
                        .param(
                                "nuevaMarca",
                                "E2E Brand"
                        )
        )
        .andExpect(
                status().is3xxRedirection()
        );

        entityManager.flush();
        entityManager.clear();

        Store commerceStore =
                storeRepository
                        .findByDominio(finalDomain)
                        .orElseThrow();

        var createdProduct =
                productoRepository
                        .findByProductNameAndStore(
                                productName,
                                commerceStore
                        )
                        .orElseThrow();

        assertThat(createdProduct.getStockSimple())
                .isEqualTo(10);

        MvcResult commerceStorefrontResult =
                mockMvc.perform(
                        get("/")
                                .header(
                                        "X-Forwarded-Host",
                                        finalDomain
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andReturn();

        assertThat(
                commerceStorefrontResult
                        .getResponse()
                        .getContentAsString()
        )
                .contains(productName);

        String checkoutJson = """
                {
                  "customer": {
                    "fullName": "E2E Commerce Customer",
                    "email": "commerce-e2e@webempresarial.test",
                    "phone": "2221234567",
                    "address": "Avenida E2E 123 Puebla"
                  },
                  "cart": [
                    {
                      "productId": %d,
                      "varianteId": null,
                      "quantity": 2
                    }
                  ],
                  "paymentMethod": "TRANSFER",
                  "couponCode": null
                }
                """.formatted(
                        createdProduct.getId()
                );

        mockMvc.perform(
                post("/api/checkout")
                        .header(
                                "X-Forwarded-Host",
                                finalDomain
                        )
                        .with(csrf())
                        .contentType(
                                "application/json"
                        )
                        .content(checkoutJson)
        )
        .andExpect(
                status().isCreated()
        );

        entityManager.flush();
        entityManager.clear();

        Store storeAfterCheckout =
                storeRepository
                        .findByDominio(finalDomain)
                        .orElseThrow();

        var productAfterCheckout =
                productoRepository
                        .findByProductNameAndStore(
                                productName,
                                storeAfterCheckout
                        )
                        .orElseThrow();

        assertThat(productAfterCheckout.getStockSimple())
                .isEqualTo(8);

        var createdOrder =
                orderRepository
                        .findAllWithCliente(
                                storeAfterCheckout
                        )
                        .stream()
                        .filter(order ->
                                "commerce-e2e@webempresarial.test"
                                        .equals(
                                                order.getCustomerEmail()
                                        )
                        )
                        .findFirst()
                        .orElseThrow();

        assertThat(createdOrder.getPaymentMethod())
                .isEqualTo(
                        com.webempresarial.store.commerce.domain.order.Order.PaymentMethod.TRANSFER
                );

        assertThat(createdOrder.getPaymentStatus())
                .isEqualTo(
                        com.webempresarial.store.commerce.domain.order.PaymentStatus.PENDING
                );

        assertThat(createdOrder.isStockReduced())
                .isTrue();

        MvcResult ordersResult =
                mockMvc.perform(
                        get("/orders")
                                .session(session)
                                .header(
                                        "X-Forwarded-Host",
                                        finalDomain
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        view().name(
                                "admin/orders"
                        )
                )
                .andReturn();

        assertThat(
                ordersResult
                        .getResponse()
                        .getContentAsString()
        )
                .contains(
                        "E2E Commerce Customer"
                );

        /*
         * ==================================================
         * ORDER LIFECYCLE - FULFILLMENT
         * ==================================================
         *
         * El checkout por transferencia ya descontó stock.
         * Confirmar el pago NO debe volver a descontarlo.
         */

        Long fulfilledOrderId =
                createdOrder.getId();

        mockMvc.perform(
                post(
                        "/orders/{id}/confirm-payment",
                        fulfilledOrderId
                )
                        .session(session)
                        .header(
                                "X-Forwarded-Host",
                                finalDomain
                        )
                        .with(csrf())
        )
        .andExpect(
                status().is3xxRedirection()
        );

        entityManager.flush();
        entityManager.clear();

        Store storeAfterPayment =
                storeRepository
                        .findByDominio(finalDomain)
                        .orElseThrow();

        var paidOrder =
                orderRepository
                        .findByIdFullAndStore(
                                fulfilledOrderId,
                                storeAfterPayment
                        )
                        .orElseThrow();

        assertThat(paidOrder.getPaymentStatus())
                .isEqualTo(
                        com.webempresarial.store.commerce.domain.order.PaymentStatus.PAID
                );

        assertThat(paidOrder.getOrderStatus())
                .isEqualTo(
                        com.webempresarial.store.commerce.domain.order.OrderStatus.PROCESSED
                );

        assertThat(paidOrder.isStockReduced())
                .isTrue();

        var productAfterPayment =
                productoRepository
                        .findByProductNameAndStore(
                                productName,
                                storeAfterPayment
                        )
                        .orElseThrow();

        /*
         * Sigue en 8: confirmar transferencia no puede
         * descontar por segunda vez.
         */
        assertThat(productAfterPayment.getStockSimple())
                .isEqualTo(8);

        /*
         * Registrar envío.
         */
        mockMvc.perform(
                post("/orders/update-shipping")
                        .session(session)
                        .header(
                                "X-Forwarded-Host",
                                finalDomain
                        )
                        .with(csrf())
                        .param(
                                "orderId",
                                fulfilledOrderId.toString()
                        )
                        .param(
                                "courier",
                                "DHL"
                        )
                        .param(
                                "trackingNumber",
                                "E2E-TRACK-001"
                        )
        )
        .andExpect(
                status().is3xxRedirection()
        );

        entityManager.flush();
        entityManager.clear();

        Store storeAfterShipping =
                storeRepository
                        .findByDominio(finalDomain)
                        .orElseThrow();

        var shippedOrder =
                orderRepository
                        .findByIdFullAndStore(
                                fulfilledOrderId,
                                storeAfterShipping
                        )
                        .orElseThrow();

        assertThat(shippedOrder.getOrderStatus())
                .isEqualTo(
                        com.webempresarial.store.commerce.domain.order.OrderStatus.SHIPPED
                );

        assertThat(shippedOrder.getCarrier())
                .isEqualTo("DHL");

        assertThat(shippedOrder.getTrackingNumber())
                .isEqualTo("E2E-TRACK-001");

        /*
         * Marcar como entregada mediante el endpoint AJAX
         * utilizado por la administración.
         */
        mockMvc.perform(
                post(
                        "/orders/{id}/status-ajax",
                        fulfilledOrderId
                )
                        .session(session)
                        .header(
                                "X-Forwarded-Host",
                                finalDomain
                        )
                        .with(csrf())
                        .contentType(
                                "application/json"
                        )
                        .content(
                                """
                                {
                                  "status": "DELIVERED"
                                }
                                """
                        )
        )
        .andExpect(
                status().isOk()
        );

        entityManager.flush();
        entityManager.clear();

        Store storeAfterDelivery =
                storeRepository
                        .findByDominio(finalDomain)
                        .orElseThrow();

        var deliveredOrder =
                orderRepository
                        .findByIdFullAndStore(
                                fulfilledOrderId,
                                storeAfterDelivery
                        )
                        .orElseThrow();

        assertThat(deliveredOrder.getOrderStatus())
                .isEqualTo(
                        com.webempresarial.store.commerce.domain.order.OrderStatus.DELIVERED
                );

        /*
         * ==================================================
         * ORDER LIFECYCLE - CANCELLATION
         * ==================================================
         *
         * Segundo pedido: descontamos 3 unidades durante
         * checkout y comprobamos que cancelarlo antes del
         * pago restaura exactamente esas 3.
         */

        String cancellationCheckoutJson = """
                {
                  "customer": {
                    "fullName": "E2E Cancellation Customer",
                    "email": "cancel-e2e@webempresarial.test",
                    "phone": "2227654321",
                    "address": "Avenida Cancelacion 456 Puebla"
                  },
                  "cart": [
                    {
                      "productId": %d,
                      "varianteId": null,
                      "quantity": 3
                    }
                  ],
                  "paymentMethod": "TRANSFER",
                  "couponCode": null
                }
                """.formatted(
                        productAfterPayment.getId()
                );

        mockMvc.perform(
                post("/api/checkout")
                        .header(
                                "X-Forwarded-Host",
                                finalDomain
                        )
                        .with(csrf())
                        .contentType(
                                "application/json"
                        )
                        .content(cancellationCheckoutJson)
        )
        .andExpect(
                status().isCreated()
        );

        entityManager.flush();
        entityManager.clear();

        Store storeBeforeCancellation =
                storeRepository
                        .findByDominio(finalDomain)
                        .orElseThrow();

        var productBeforeCancellation =
                productoRepository
                        .findByProductNameAndStore(
                                productName,
                                storeBeforeCancellation
                        )
                        .orElseThrow();

        assertThat(productBeforeCancellation.getStockSimple())
                .isEqualTo(5);

        var cancellableOrder =
                orderRepository
                        .findAllWithCliente(
                                storeBeforeCancellation
                        )
                        .stream()
                        .filter(order ->
                                "cancel-e2e@webempresarial.test"
                                        .equals(
                                                order.getCustomerEmail()
                                        )
                        )
                        .findFirst()
                        .orElseThrow();

        assertThat(cancellableOrder.getPaymentStatus())
                .isEqualTo(
                        com.webempresarial.store.commerce.domain.order.PaymentStatus.PENDING
                );

        assertThat(cancellableOrder.isStockReduced())
                .isTrue();

        mockMvc.perform(
                post(
                        "/orders/{id}/cancel",
                        cancellableOrder.getId()
                )
                        .session(session)
                        .header(
                                "X-Forwarded-Host",
                                finalDomain
                        )
                        .with(csrf())
        )
        .andExpect(
                status().is3xxRedirection()
        );

        entityManager.flush();
        entityManager.clear();

        Store storeAfterCancellation =
                storeRepository
                        .findByDominio(finalDomain)
                        .orElseThrow();

        var cancelledOrder =
                orderRepository
                        .findByIdFullAndStore(
                                cancellableOrder.getId(),
                                storeAfterCancellation
                        )
                        .orElseThrow();

        assertThat(cancelledOrder.getOrderStatus())
                .isEqualTo(
                        com.webempresarial.store.commerce.domain.order.OrderStatus.CANCELLED
                );

        assertThat(cancelledOrder.isStockReduced())
                .isFalse();

        var productAfterCancellation =
                productoRepository
                        .findByProductNameAndStore(
                                productName,
                                storeAfterCancellation
                        )
                        .orElseThrow();

        /*
         * 8 -> checkout de 3 = 5 -> cancelación = 8.
         */
        assertThat(productAfterCancellation.getStockSimple())
                .isEqualTo(8);
    }

    private Long provisionedStoreId(
            String domain
    ) {

        return storeRepository
                .findByDominio(domain)
                .orElseThrow()
                .getId();
    }

}
