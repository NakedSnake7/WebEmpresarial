package com.webempresarial.store.feature.registry;

import com.webempresarial.store.dto.dashboard.DashboardWidgetDTO;
import com.webempresarial.store.feature.ModuleDefinition;
import com.webempresarial.store.feature.dashboard.DashboardWidgetDefinition;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.FeatureAccessService;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class DashboardRegistry {

    private final List<DashboardWidgetDefinition> widgets = new ArrayList<>();
    private final FeatureAccessService featureAccessService;

    public DashboardRegistry(FeatureAccessService featureAccessService) {
        this.featureAccessService = featureAccessService;
    }

    public void register(ModuleDefinition moduleDefinition) {
        if (moduleDefinition == null) {
            return;
        }

        widgets.addAll(moduleDefinition.getDashboardWidgets());
    }

    public List<DashboardWidgetDTO> widgets(Store store) {
        return widgets.stream()
                .sorted(Comparator.comparingInt(DashboardWidgetDefinition::order))
                .map(widget -> toDto(widget, store))
                .toList();
    }

    private DashboardWidgetDTO toDto(
            DashboardWidgetDefinition widget,
            Store store
    ) {
        boolean locked = !featureAccessService.canUse(
                store,
                widget.feature()
        );

        return new DashboardWidgetDTO(
                widget.title(),
                widget.subtitle(),
                locked ? "🔒" : widget.icon(),
                locked
                        ? "/admin/upgrade?feature=" + widget.feature().name()
                        : widget.url(),
                widget.feature(),
                locked,
                locked ? "Upgrade" : ""
        );
    }
}