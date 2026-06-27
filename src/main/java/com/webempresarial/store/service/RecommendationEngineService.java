package com.webempresarial.store.service;

import com.webempresarial.store.dto.saas.ExecutiveMetricsDTO;
import com.webempresarial.store.dto.saas.SaaSRecommendationDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationEngineService {

    private final ExecutiveMetricsService executiveMetricsService;
    private final SaasMetricsService saasMetricsService;

    public RecommendationEngineService(
            ExecutiveMetricsService executiveMetricsService,
            SaasMetricsService saasMetricsService
    ) {
        this.executiveMetricsService = executiveMetricsService;
        this.saasMetricsService = saasMetricsService;
    }

    public List<SaaSRecommendationDTO> getRecommendations() {

        ExecutiveMetricsDTO executive =
                executiveMetricsService.getMetrics();

        var metrics =
                saasMetricsService.getMetrics();

        List<SaaSRecommendationDTO> recommendations =
                new ArrayList<>();

        if (executive.getChurnRatePercent()
                .compareTo(BigDecimal.valueOf(5)) > 0) {

            recommendations.add(new SaaSRecommendationDTO(
                    "CHURN",
                    "Churn elevado",
                    "La tasa de cancelación está por encima del 5%. Revisa clientes cancelados y tiendas con baja actividad.",
                    "Ver suscripciones",
                    "/admin/subscriptions",
                    "danger"
            ));
        }

        if (executive.getPastDueCustomers() > 0) {

            recommendations.add(new SaaSRecommendationDTO(
                    "PAST_DUE",
                    "Pagos fallidos detectados",
                    "Hay " + executive.getPastDueCustomers() + " cliente(s) con pagos vencidos. Conviene contactarlos antes de que cancelen.",
                    "Ver suscripciones",
                    "/admin/subscriptions",
                    "warning"
            ));
        }

        if (executive.getTrialCustomers() > 0) {

            recommendations.add(new SaaSRecommendationDTO(
                    "TRIALS",
                    "Trials activos",
                    "Tienes " + executive.getTrialCustomers() + " tienda(s) en periodo de prueba. Es buen momento para hacer seguimiento comercial.",
                    "Ver tiendas",
                    "/admin/stores",
                    "info"
            ));
        }

        if (metrics.getBasicStores() > metrics.getProStores() + metrics.getPremiumStores()) {

            recommendations.add(new SaaSRecommendationDTO(
                    "UPSELL",
                    "Oportunidad de upgrade",
                    "La mayoría de tus tiendas están en Basic. Puedes impulsar upgrades a Pro mostrando CRM, Pipeline y Propuestas.",
                    "Ver billing",
                    "/admin/billing",
                    "success"
            ));
        }

        if (metrics.getStripeConnectedStores() < metrics.getActiveStores()) {

            long pending =
                    metrics.getActiveStores() - metrics.getStripeConnectedStores();

            recommendations.add(new SaaSRecommendationDTO(
                    "STRIPE_CONNECT",
                    "Tiendas sin Stripe Connect",
                    pending + " tienda(s) activas aún no conectan Stripe. Esto puede limitar sus ventas.",
                    "Ver tiendas",
                    "/admin/stores",
                    "warning"
            ));
        }

        if (recommendations.isEmpty()) {

            recommendations.add(new SaaSRecommendationDTO(
                    "HEALTHY",
                    "Todo se ve estable",
                    "No hay alertas críticas por ahora. El SaaS se mantiene saludable.",
                    "Ver dashboard",
                    "/admin/saas",
                    "success"
            ));
        }

        return recommendations;
    }
}