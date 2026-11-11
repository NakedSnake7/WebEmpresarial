package com.webempresarial.store.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AdminPasswordResetRateLimiterTest {

    @Test
    void permitsFiveRequestsThenRejects() {

        var limiter = new AdminPasswordResetRateLimiter(
                Clock.fixed(
                        Instant.parse(
                                "2026-11-10T12:00:00Z"
                        ),
                        ZoneOffset.UTC
                )
        );

        for (int i = 0; i < 5; i++) {
            assertTrue(
                    limiter.allowRecoveryRequest("127.0.0.1")
            );
        }

        assertFalse(
                limiter.allowRecoveryRequest("127.0.0.1")
        );

        assertTrue(
                limiter.allowRecoveryRequest("127.0.0.2")
        );
    }

    @Test
    void separatesRequestAndRedemptionBudgets() {

        var limiter = new AdminPasswordResetRateLimiter();

        for (int i = 0; i < 5; i++) {
            assertTrue(
                    limiter.allowRecoveryRequest("192.0.2.10")
            );
        }

        assertFalse(
                limiter.allowRecoveryRequest("192.0.2.10")
        );

        for (int i = 0; i < 20; i++) {
            assertTrue(
                    limiter.allowRecoveryCompletion(
                            "192.0.2.10"
                    )
            );
        }

        assertFalse(
                limiter.allowRecoveryCompletion(
                        "192.0.2.10"
                )
        );
    }

    @Test
    void opensNewWindowAfterFifteenMinutes() {

        class AdjustableClock extends Clock {

            private Instant current =
                    Instant.parse(
                            "2026-11-10T12:00:00Z"
                    );

            @Override
            public ZoneId getZone() {
                return ZoneOffset.UTC;
            }

            @Override
            public Clock withZone(ZoneId zone) {
                return this;
            }

            @Override
            public Instant instant() {
                return current;
            }

            void advance(Duration duration) {
                current = current.plus(duration);
            }
        }

        AdjustableClock clock = new AdjustableClock();

        var limiter =
                new AdminPasswordResetRateLimiter(clock);

        for (int i = 0; i < 5; i++) {
            assertTrue(
                    limiter.allowRecoveryRequest("192.0.2.20")
            );
        }

        assertFalse(
                limiter.allowRecoveryRequest("192.0.2.20")
        );

        clock.advance(Duration.ofMinutes(15));

        assertTrue(
                limiter.allowRecoveryRequest("192.0.2.20")
        );
    }

    @Test
    void capacityDoesNotEvictActiveOrigins() {

        AtomicReference<Instant> current =
                new AtomicReference<>(
                        Instant.parse(
                                "2026-11-10T12:00:00Z"
                        )
                );

        Clock clock = new Clock() {

            @Override
            public ZoneId getZone() {
                return ZoneOffset.UTC;
            }

            @Override
            public Clock withZone(ZoneId zone) {
                return this;
            }

            @Override
            public Instant instant() {
                return current.get();
            }
        };

        var limiter =
                new AdminPasswordResetRateLimiter(clock);

        String original = "203.0.113.77";

        /*
         * Consumir todo el presupuesto del primer
         * origen y comprobar su bloqueo.
         */
        for (int i = 0; i < 5; i++) {
            assertTrue(
                    limiter.allowRecoveryRequest(original)
            );
        }

        assertFalse(
                limiter.allowRecoveryRequest(original)
        );

        /*
         * Completar las 10 000 entradas disponibles.
         */
        for (int i = 0; i < 9_999; i++) {

            String remote =
                    "198.51."
                    + (i / 256)
                    + "."
                    + (i % 256);

            assertTrue(
                    limiter.allowRecoveryRequest(remote),
                    "Debe aceptar el origen " + i
            );
        }

        /*
         * Un origen nuevo no puede expulsar al
         * primero ni recuperar sus intentos.
         */
        String unseen = "203.0.113.222";

        assertFalse(
                limiter.allowRecoveryRequest(unseen)
        );

        assertFalse(
                limiter.allowRecoveryRequest(original)
        );

        /*
         * Tras 15 minutos se pueden limpiar
         * las ventanas expiradas.
         */
        current.set(
                current.get().plus(
                        Duration.ofMinutes(15)
                )
        );

        assertTrue(
                limiter.allowRecoveryRequest(unseen)
        );

        assertTrue(
                limiter.allowRecoveryRequest(original)
        );
    }

}
