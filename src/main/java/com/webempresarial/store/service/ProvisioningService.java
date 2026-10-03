package com.webempresarial.store.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private final ApplicationEventPublisher eventPublisher;

    public ProvisioningService(
            StoreRepository storeRepository,
            SubscriptionRepository subscriptionRepository,
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            AdminAccountActivationService adminAccountActivationService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.storeRepository = storeRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminAccountActivationService = adminAccountActivationService;
        this.eventPublisher = eventPublisher;
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

        String normalizedDomain = normalizeDomain(domain);
        String finalDomain = normalizedDomain + ".web-empresarial.com";

        var existingStore = storeRepository.findByDominio(finalDomain);

        if (existingStore.isPresent()) {
            return existingStore.get();
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

        createStoreAdmin(savedStore, ownerName, normalizedEmail);

        return savedStore;
    }

    private void createStoreAdmin(
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
        admin.setRole(AdminRole.STORE_ADMIN);

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
