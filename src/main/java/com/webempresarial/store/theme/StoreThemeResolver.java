package com.webempresarial.store.theme;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.model.ThemeType;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class StoreThemeResolver {

    private static final String DEFAULT_THEME = "WebEmpresarial";

    private final StoreResolver storeResolver;

    public StoreThemeResolver(StoreResolver storeResolver) {
        this.storeResolver = storeResolver;
    }

    public String getTheme(HttpServletRequest request) {

        try {
            Store store = storeResolver.getCurrentStore(request);
            return resolveTheme(store);

        } catch (Exception e) {
            return DEFAULT_THEME;
        }
    }

    public String resolveTheme(Store store) {

        if (store == null) {
            return DEFAULT_THEME;
        }

        if (store.getThemeType() != null) {

            ThemeType themeType = store.getThemeType();

            return switch (themeType) {

                case BASIC -> "basic";

                case PRO -> "pro";

                case PREMIUM -> "premium";

                case CUSTOM -> resolveCustomTheme(store);
            };
        }

        return resolveThemeByPlan(store.getPlan());
    }

    private String resolveCustomTheme(Store store) {

        if (store.getTheme() != null && !store.getTheme().isBlank()) {
            return store.getTheme();
        }

        return DEFAULT_THEME;
    }

    private String resolveThemeByPlan(StorePlan plan) {

        if (plan == null) {
            return DEFAULT_THEME;
        }

        return switch (plan) {
            case BASIC -> "basic";
            case PRO -> "pro";
            case PREMIUM -> "premium";
        };
    }

    public String view(
            HttpServletRequest request,
            String page
    ) {
        return "themes/" + getTheme(request) + "/" + page;
    }

    public String fragment(
            HttpServletRequest request,
            String fragment
    ) {
        return "themes/" + getTheme(request) + "/fragments/" + fragment;
    }
}