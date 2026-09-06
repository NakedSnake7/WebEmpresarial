package com.webempresarial.store.service;

import com.webempresarial.store.dto.saas.StoreHealthDTO; 
import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.SubscriptionStatus;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.repository.FeatureUsageRepository;
import com.webempresarial.store.repository.LeadRepository;
import com.webempresarial.store.commerce.application.order.OrderMetricsQueryService;
import com.webempresarial.store.repository.ProductoRepository;
import com.webempresarial.store.repository.StoreRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class HealthScoreService {

    private final StoreRepository storeRepository;
    private final ProductoRepository productoRepository;
    private final OrderMetricsQueryService orderMetricsQueryService;
    private final LeadRepository leadRepository;
    private final FeatureUsageRepository featureUsageRepository;

    public HealthScoreService(
            StoreRepository storeRepository,
            ProductoRepository productoRepository,
            OrderMetricsQueryService orderMetricsQueryService,
            LeadRepository leadRepository,
            FeatureUsageRepository featureUsageRepository
    ) {
        this.storeRepository = storeRepository;
        this.productoRepository = productoRepository;
        this.orderMetricsQueryService = orderMetricsQueryService;
        this.leadRepository = leadRepository;
        this.featureUsageRepository = featureUsageRepository;
    }
    public List<StoreHealthDTO> calculateAllStores() {
        return storeRepository.findAllWithSubscription()
                .stream()
                .map(this::calculate)
                .sorted(Comparator.comparing(StoreHealthDTO::getOverallScore))
                .toList();
    }
    
    private int calculateEcommerceScore(Store store, StoreHealthDTO dto) {
        int score = 0;

        long totalProducts = productoRepository.countByStoreId(store.getId());
        long visibleProducts = productoRepository.countByStoreIdAndVisibleEnMenuTrue(store.getId());
        long orders =
                orderMetricsQueryService.countOrdersByStore(
                        store.getId()
                );

        if (totalProducts > 0) {
            score += 6;
        } else {
            dto.getRecommendations().add("Agrega productos a la tienda.");
        }

        if (visibleProducts >= 5) {
            score += 5;
        } else {
            dto.getRecommendations().add("Publica al menos 5 productos visibles.");
        }

        if (orders > 0) {
            score += 6;
        } else {
            dto.getRecommendations().add("Aún no hay pedidos registrados.");
        }

        if (store.isStripeConnected()) {
            score += 3;
        }

        return Math.min(score, 20);
    }

    private int calculateCrmScore(Store store, StoreHealthDTO dto) {
        int score = 0;

        long leads = leadRepository.countAllLeads(store.getId());
        long closedLeads = leadRepository.countClosedLeads(store.getId());

        if (leads > 0) {
            score += 7;
        } else {
            dto.getRecommendations().add("Empieza a capturar leads desde el CRM.");
        }

        if (closedLeads > 0) {
            score += 5;
        }

        if (featureUsageRepository.countByStoreAndFeature(store, Feature.CRM) > 0) {
            score += 3;
        }

        if (featureUsageRepository.countByStoreAndFeature(store, Feature.PIPELINE) > 0) {
            score += 3;
        }

        if (featureUsageRepository.countByStoreAndFeature(store, Feature.ANALYTICS) > 0) {
            score += 2;
        }

        return Math.min(score, 20);
    }
    
    private int calculateActivityScore(Store store, StoreHealthDTO dto) {

        int score = 0;

        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);

        long totalEvents =
        		featureUsageRepository.countByStoreAndUsedAtAfter(
        		        store,
        		        last30Days
        		);

        if (totalEvents >= 500) {

            score += 15;

        } else if (totalEvents >= 250) {

            score += 12;

        } else if (totalEvents >= 100) {

            score += 9;

        } else if (totalEvents >= 50) {

            score += 6;

        } else if (totalEvents > 0) {

            score += 3;

        } else {

            dto.getRecommendations().add(
                    "La tienda muestra poca actividad durante el último mes."
            );

        }

        return score;
    }
    
    private int calculateAutomationScore(
            Store store,
            StoreHealthDTO dto
    ) {

        int score = 0;

        if (featureUsageRepository.countByStoreAndFeature(
                store,
                Feature.AUTOMATIONS) > 0) {

            score += 4;

        } else {

            dto.getRecommendations().add(
                    "Activa Automatizaciones."
            );
        }

        if (featureUsageRepository.countByStoreAndFeature(
                store,
                Feature.EMAIL_MARKETING) > 0) {

            score += 3;

        } else {

            dto.getRecommendations().add(
                    "Configura campañas de Email Marketing."
            );
        }

        if (featureUsageRepository.countByStoreAndFeature(
                store,
                Feature.WHATSAPP_AUTOMATION) > 0) {

            score += 3;

        } else {

            dto.getRecommendations().add(
                    "Implementa automatizaciones por WhatsApp."
            );
        }

        return Math.min(score, 10);
    }

    public StoreHealthDTO calculate(Store store) {
        StoreHealthDTO dto = new StoreHealthDTO();

        dto.setStoreId(store.getId());
        dto.setStoreName(store.getNombre());
        dto.setDomain(store.getDominio());

        int branding = calculateBrandingScore(store, dto);
        int billing = calculateBillingScore(store, dto);
        int ecommerce = calculateEcommerceScore(store, dto);
        int crm = calculateCrmScore(store, dto);
        int automation = calculateAutomationScore(store, dto);
        int activity = calculateActivityScore(store, dto);
        
        dto.setBrandingScore(branding);
        dto.setBillingScore(billing);
        dto.setEcommerceScore(ecommerce);
        dto.setCrmScore(crm);
        dto.setAutomationScore(automation);
        dto.setActivityScore(activity);

        int total = branding + billing + ecommerce + crm + automation + activity;
        dto.setOverallScore(total);
        dto.setStatus(resolveStatus(total));

        return dto;
    }

    private int calculateBrandingScore(Store store, StoreHealthDTO dto) {
        int score = 0;

        if (hasText(store.getLogoUrl())) {
            score += 4;
        } else {
            dto.getRecommendations().add("Agrega un logo a la tienda.");
        }

        if (hasText(store.getFaviconUrl())) {
            score += 2;
        } else {
            dto.getRecommendations().add("Configura un favicon.");
        }

        if (hasText(store.getHeroImageUrl())) {
            score += 3;
        } else {
            dto.getRecommendations().add("Agrega una imagen principal o hero.");
        }

        if (hasText(store.getPrimaryColor())
                && hasText(store.getSecondaryColor())
                && hasText(store.getAccentColor())) {
            score += 3;
        }

        if (hasText(store.getSlogan())) {
            score += 3;
        } else {
            dto.getRecommendations().add("Agrega un slogan comercial.");
        }

        return Math.min(score, 15);
    }

    private int calculateBillingScore(Store store, StoreHealthDTO dto) {
        int score = 0;

        Subscription subscription = store.getSubscription();

        if (subscription == null) {
            dto.getRecommendations().add("Crea una suscripción para esta tienda.");
            return 0;
        }

        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            score += 8;
        } else if (subscription.getStatus() == SubscriptionStatus.TRIAL) {
            score += 5;
            dto.getRecommendations().add("Convierte el trial en suscripción activa.");
        } else {
            dto.getRecommendations().add("Revisa el estado de suscripción.");
        }

        if (subscription.isBillingExempt()) {
            score += 4;
        }

        if (hasText(subscription.getStripeCustomerId())
                && hasText(subscription.getStripeSubscriptionId())) {
            score += 4;
        }

        if (store.isStripeConnected()) {
            score += 4;
        } else {
            dto.getRecommendations().add("Conecta Stripe para recibir pagos.");
        }

        return Math.min(score, 20);
    }

    private String resolveStatus(int score) {
        if (score >= 80) return "HEALTHY";
        if (score >= 50) return "WARNING";
        return "RISK";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}