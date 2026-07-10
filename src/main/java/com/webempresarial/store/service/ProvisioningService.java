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

import jakarta.transaction.Transactional;

@Service
public class ProvisioningService {

    private final StoreRepository storeRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public ProvisioningService(
            StoreRepository storeRepository,
            SubscriptionRepository subscriptionRepository,
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.storeRepository = storeRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
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

    	LocalDateTime now = LocalDateTime.now();

    	Store store = new Store();
    	store.setNombre(companyName);
    	store.setTheme("default");
    	store.setDominio(finalDomain);
    	store.setActiva(true);
    	store.setPlan(plan);
    	store.setContactName(ownerName);
    	store.setCompanyEmail(email);
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

    	createStoreAdmin(savedStore, ownerName, email);

    	return savedStore;
    }

    private void createStoreAdmin(
            Store store,
            String ownerName,
            String email
    ) {
        if (adminUserRepository.existsByEmail(email)) {
            return;
        }

        AdminUser admin = new AdminUser();

        admin.setFullName(ownerName);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(generateTemporaryPassword()));
        admin.setEnabled(true);
        admin.setStore(store);

                
        admin.setRole(AdminRole.STORE_ADMIN);
        
        
        adminUserRepository.save(admin);
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
}