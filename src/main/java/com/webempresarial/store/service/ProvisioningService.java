package com.webempresarial.store.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.model.SubscriptionStatus;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.repository.SubscriptionRepository;

import com.webempresarial.store.entity.AdminAccountActivationToken;
import com.webempresarial.store.event.AdminAccountCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;

import jakarta.transaction.Transactional;

@Service
public class ProvisioningService {

    private final StoreRepository storeRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminAccountActivationService adminAccountActivationService;
    private final StoreSettingsService storeSettingsService;
    private final ApplicationEventPublisher eventPublisher;

    public ProvisioningService(
            StoreRepository storeRepository,
            SubscriptionRepository subscriptionRepository,
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            AdminAccountActivationService adminAccountActivationService,
            StoreSettingsService storeSettingsService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.storeRepository = storeRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminAccountActivationService = adminAccountActivationService;
        this.storeSettingsService = storeSettingsService;
        this.eventPublisher = eventPublisher;
    }


    public void validateCheckoutEligibility(
            String domain,
            String email
    ) {

        String normalizedDomain =
                normalizeDomain(domain);

        String finalDomain =
                normalizedDomain
                        + ".web-empresarial.com";

        if (storeRepository.existsByDominio(finalDomain)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El dominio ya está en uso"
            );
        }

        String normalizedEmail =
                normalizeEmail(email);

        if (adminUserRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo ya está asociado a una cuenta administrativa"
            );
        }
    }

    @Transactional
    public Store provisionStoreFromCheckout(
            String companyName,
            String domain,
            String ownerName,
            String email,
            StorePlan plan,
            String stripeCustomerId,
            String stripeSubscriptionId,
            String stripePriceId
    ) {

        if (stripeSubscriptionId == null
                || stripeSubscriptionId.isBlank()) {

            throw new IllegalArgumentException(
                    "stripeSubscriptionId es obligatorio"
            );
        }

        String normalizedDomain =
                normalizeDomain(domain);

        String finalDomain =
                normalizedDomain
                        + ".web-empresarial.com";

        /*
         * La Stripe Subscription es la identidad financiera
         * de la adquisición.
         *
         * Si ya existe y pertenece al mismo tenant,
         * estamos ante un retry idempotente legítimo.
         *
         * Si pertenece a otro tenant, rechazamos la operación.
         */
        var existingStripeSubscription =
                subscriptionRepository
                        .findByStripeSubscriptionId(
                                stripeSubscriptionId
                        );

        if (existingStripeSubscription.isPresent()) {

            Store subscribedStore =
                    existingStripeSubscription
                            .get()
                            .getStore();

            if (subscribedStore != null
                    && finalDomain.equals(
                            subscribedStore.getDominio()
                    )) {

                return subscribedStore;
            }

            throw new IllegalStateException(
                    "La suscripción Stripe ya está asociada "
                    + "a otro tenant"
            );
        }

        /*
         * Si el dominio ya existe pero la Stripe Subscription
         * recibida es distinta, NO es un retry.
         *
         * Es otra adquisición intentando ocupar un tenant
         * que ya tiene identidad financiera propia.
         */
        var existingStore =
                storeRepository
                        .findByDominio(finalDomain);

        if (existingStore.isPresent()) {
            throw new IllegalStateException(
                    "El dominio ya está asociado "
                    + "a otra suscripción Stripe"
            );
        }

        String normalizedEmail =
                normalizeEmail(email);

        if (adminUserRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalStateException(
                    "Ya existe una cuenta administrativa con este correo"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        Store store = new Store();
        store.setNombre(companyName);
        store.setTheme("default");
        store.setDominio(finalDomain);
        store.setActiva(true);
        store.setPlan(plan);
        store.setContactName(ownerName);
        store.setCompanyEmail(normalizedEmail);
        store.setCurrency("MXN");

        Store savedStore = storeRepository.save(store);

        storeSettingsService.createDefaults(savedStore);

        Subscription subscription = new Subscription();
        subscription.setStore(savedStore);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStripeCustomerId(stripeCustomerId);
        subscription.setStripeSubscriptionId(stripeSubscriptionId);
        subscription.setStripePriceId(stripePriceId);
        subscription.setStartsAt(now);
        subscription.setEndsAt(null);
        subscription.setCurrentPeriodStart(now);
        subscription.setCurrentPeriodEnd(now.plusMonths(1));
        subscription.setNextBillingDate(now.plusMonths(1));

        subscriptionRepository.save(subscription);

        createStoreOwner(savedStore, ownerName, normalizedEmail);

        return savedStore;
    }

    private void createStoreOwner(
            Store store,
            String ownerName,
            String email
    ) {



        AdminUser admin = new AdminUser();

        admin.setFullName(ownerName);
        admin.setEmail(email);

        /*
         * Placeholder inaccesible.
         * El propietario establecerá su contraseña
         * mediante el flujo de activación.
         */
        admin.setPassword(
                passwordEncoder.encode(
                        generateTemporaryPassword()
                )
        );

        /*
         * No puede autenticarse hasta activar
         * personalmente la cuenta.
         */
        admin.setEnabled(false);

        admin.setStore(store);
        /*
         * El usuario creado durante el checkout representa
         * al propietario contractual del tenant.
         */
        admin.setRole(AdminRole.STORE_OWNER);

        AdminUser savedAdmin =
                adminUserRepository.save(admin);

        AdminAccountActivationToken activationToken =
                adminAccountActivationService
                        .createToken(savedAdmin);

        eventPublisher.publishEvent(
                new AdminAccountCreatedEvent(
                        savedAdmin.getId(),
                        savedAdmin.getEmail(),
                        savedAdmin.getFullName(),
                        store.getNombre(),
                        store.getDominio(),
                        activationToken.getToken()
                )
        );
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12);
    }

    private String normalizeDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            throw new RuntimeException("El dominio no puede estar vacío");
        }

        return domain
                .trim()
                .toLowerCase()
                .replace("https://", "")
                .replace("http://", "")
                .replace(".web-empresarial.com", "")
                .replace("/", "")
                .replace(" ", "-");
    }

    private String normalizeEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "El correo no puede estar vacío"
            );
        }

        return email
                .trim()
                .toLowerCase();
    }
}
