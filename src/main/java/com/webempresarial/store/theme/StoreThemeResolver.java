package com.webempresarial.store.theme;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StoreThemeResolver {

    private static final String DEFAULT_THEME = "WebEmpresarial";

    private static final Map<String, String> DOMAIN_THEME = Map.of(

    	    // =========================
    	    // PRODUCCIÓN
    	    // =========================

    	    "web-empresarial.com", "WebEmpresarial",
    	    "www.web-empresarial.com", "WebEmpresarial",

    	    "stride.web-empresarial.com", "stride",
    	    "punchbarley.web-empresarial.com", "punchbarley",

    	    // =========================
    	    // LOCAL
    	    // =========================

    	    "webempresarial.local", "WebEmpresarial",
    	    "stride.local", "stride",
    	    "punchbarley.local", "punchbarley"
    	);

    public String getTheme(HttpServletRequest request) {

        String host = request.getHeader("X-Forwarded-Host");

        if (host == null || host.isBlank()) {
            host = request.getServerName();
        }

        if (host == null || host.isBlank()) {
            return DEFAULT_THEME;
        }

        host = host.toLowerCase().split(":")[0];

        // localhost directo
        if (
            host.equals("localhost") ||
            host.equals("127.0.0.1")
        ) {
          //  return DEFAULT_THEME;
               return "punchbarley";
        }

        return DOMAIN_THEME.getOrDefault(
            host,
            DEFAULT_THEME
        );
    }

    public String view(HttpServletRequest request, String page) {
        return "themes/" + getTheme(request) + "/" + page;
    }

    public String fragment(HttpServletRequest request, String fragment) {
        return "themes/" + getTheme(request) + "/fragments/" + fragment;
    }
}