package com.webempresarial.store.theme;

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

            return storeResolver
                    .getCurrentStore(request)
                    .getTheme();

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