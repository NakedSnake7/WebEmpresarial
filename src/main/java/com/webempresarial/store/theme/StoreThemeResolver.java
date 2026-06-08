package com.webempresarial.store.theme;

import com.webempresarial.store.model.Store;
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

            if (store == null || store.getThemeType() == null) {
                return DEFAULT_THEME;
            }

            ThemeType themeType = store.getThemeType();

            return switch (themeType) {

                case BASIC -> "basic";

                case PRO -> "pro";

                case PREMIUM -> "premium";

                case CUSTOM -> {
                    if (store.getTheme() != null && !store.getTheme().isBlank()) {
                        yield store.getTheme();
                    }

                    yield DEFAULT_THEME;
                }
            };

        } catch (Exception e) {

            return DEFAULT_THEME;
        }
    }

    public String view(
            HttpServletRequest request,
            String page
    ) {

        return "themes/"
                + getTheme(request)
                + "/"
                + page;
    }

    public String fragment(
            HttpServletRequest request,
            String fragment
    ) {

        return "themes/"
                + getTheme(request)
                + "/fragments/"
                + fragment;
    }
}