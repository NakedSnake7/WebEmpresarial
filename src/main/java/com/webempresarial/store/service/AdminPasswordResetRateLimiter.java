package com.webempresarial.store.service;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AdminPasswordResetRateLimiter {

    private static final Duration WINDOW =
            Duration.ofMinutes(15);

    private static final int REQUEST_LIMIT = 5;
    private static final int REDEMPTION_LIMIT = 20;

    private static final int MAX_TRACKED_KEYS = 10_000;

    /*
     * Evita recorrer las 10 000 entradas por cada
     * solicitud nueva cuando la tabla está llena.
     */
    private static final Duration CLEANUP_INTERVAL =
            Duration.ofMinutes(1);

    private final Clock clock;

    private final Map<String, Window> windows =
            new LinkedHashMap<>();

    private Instant nextCleanupAt = Instant.MIN;

    public AdminPasswordResetRateLimiter() {
        this(Clock.systemUTC());
    }

    AdminPasswordResetRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public synchronized boolean allowRecoveryRequest(
            String remoteAddress
    ) {
        return allow(
                "request:" + normalize(remoteAddress),
                REQUEST_LIMIT
        );
    }

    public synchronized boolean allowRecoveryCompletion(
            String remoteAddress
    ) {
        return allow(
                "completion:" + normalize(remoteAddress),
                REDEMPTION_LIMIT
        );
    }

    private boolean allow(String key, int limit) {

        Instant now = clock.instant();
        Window window = windows.get(key);

        /*
         * Las ventanas activas conservan su contador,
         * incluso cuando se alcanza la capacidad.
         */
        if (window != null
                && now.isBefore(
                        window.started.plus(WINDOW)
                )) {

            if (window.attempts >= limit) {
                return false;
            }

            window.attempts++;
            return true;
        }

        /*
         * Si el origen ya estaba registrado y su
         * ventana expiró, puede comenzar otra.
         */
        if (window != null) {
            windows.remove(key);
        }

        if (windows.size() >= MAX_TRACKED_KEYS) {

            /*
             * Eliminar solamente entradas expiradas.
             * Nunca liberar una ventana activa para
             * aceptar otro origen.
             */
            if (!now.isBefore(nextCleanupAt)) {

                windows.entrySet().removeIf(
                        entry -> !now.isBefore(
                                entry.getValue()
                                        .started
                                        .plus(WINDOW)
                        )
                );

                nextCleanupAt =
                        now.plus(CLEANUP_INTERVAL);
            }

            /*
             * Fail closed: cuando no existe capacidad,
             * los nuevos orígenes no reciben intentos.
             */
            if (windows.size() >= MAX_TRACKED_KEYS) {
                return false;
            }
        }

        windows.put(key, new Window(now));
        return true;
    }

    /*
     * El controlador pasará request.getRemoteAddr().
     * No leer encabezados X-Forwarded-For directamente.
     */
    private String normalize(String value) {

        if (value == null
                || value.isBlank()
                || value.length() > 64) {
            return "unknown";
        }

        return value.trim();
    }

    private static final class Window {

        private final Instant started;
        private int attempts = 1;

        private Window(Instant started) {
            this.started = started;
        }
    }
}
