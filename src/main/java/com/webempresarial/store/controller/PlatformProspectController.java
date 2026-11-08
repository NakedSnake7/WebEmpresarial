package com.webempresarial.store.controller;

import com.webempresarial.store.dto.PlatformProspectRequest;
import com.webempresarial.store.entity.PlatformProspect;
import com.webempresarial.store.repository.PlatformProspectRepository;
import com.webempresarial.store.theme.StoreResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/platform/prospects")
public class PlatformProspectController {

    private static final Set<String> PAYMENT_CHOICES =
            Set.of("por-definir", "transferencia", "stripe");

    private static final long WINDOW_MILLIS = 60_000;
    private static final int MAX_PER_MINUTE = 6;
    // Defensa adicional por contacto, independiente de cabeceras de proxy.
    private static final long CONTACT_WINDOW_MILLIS = 15 * 60_000L;
    private static final int MAX_PER_CONTACT_WINDOW = 3;
    private static final int MAX_CONTACT_WINDOWS = 5000;

    private final StoreResolver storeResolver;
    private final PlatformProspectRepository repository;
    private final PilotPrivacyReadiness privacyReadiness;
    private final ConcurrentHashMap<String, AttemptWindow> attempts =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AttemptWindow> contactAttempts =
            new ConcurrentHashMap<>();

    private record AttemptWindow(long since, int count) {}

    public PlatformProspectController(StoreResolver storeResolver,
            PlatformProspectRepository repository,
            PilotPrivacyReadiness privacyReadiness) {
        this.storeResolver = storeResolver;
        this.repository = repository;
        this.privacyReadiness = privacyReadiness;
    }

    @PostMapping
    public ResponseEntity<Map<String, Boolean>> capture(
            @Valid @RequestBody PlatformProspectRequest dto,
            HttpServletRequest request) {

        if (!storeResolver.isPlatformRequest(request)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // No recibir datos personales hasta publicar un aviso validado.
        privacyReadiness.requireApproved();
        enforceLocalRateLimit(request.getRemoteAddr());

        // Honeypot: responder como aceptado sin almacenar mensajes de bots.
        if (dto.website() != null && !dto.website().isBlank()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of("success", true));
        }

        String form = trim(dto.form());
        if (!"main_form".equals(form) && !"hero_micro".equals(form)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Origen de formulario inválido");
        }

        String preference = trim(dto.preferenciaPagoPiloto());
        if (preference == null) preference = "por-definir";
        if (!PAYMENT_CHOICES.contains(preference)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Preferencia de pago inválida");
        }

        // Declaración del visitante: nunca autoriza precios ni cohortes.
        String hint = trim(dto.cohortHint());
        String cohortHint = "A".equalsIgnoreCase(hint) ? "A" :
                "B".equalsIgnoreCase(hint) ? "B" : "UNASSIGNED";

        // No depende de X-Forwarded-For: reduce reenvíos con el mismo contacto.
        enforceContactRateLimit(dto.whatsapp());

        PlatformProspect prospect = new PlatformProspect(
                trim(dto.nombre()),
                trim(dto.whatsapp()),
                trim(dto.empresa()),
                trim(dto.instagram()),
                trim(dto.servicio()),
                trim(dto.presupuesto()),
                trim(dto.objetivo()),
                cohortHint,
                preference,
                form
        );
        repository.save(prospect);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true));
    }

    private void enforceLocalRateLimit(String address) {
        String key = address == null ? "unknown" : address;
        long now = System.currentTimeMillis();
        if (attempts.size() >= MAX_CONTACT_WINDOWS) {
            attempts.entrySet().removeIf(entry ->
                    now - entry.getValue().since() >= WINDOW_MILLIS);
            if (!attempts.containsKey(key)
                    && attempts.size() >= MAX_CONTACT_WINDOWS) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
            }
        }
        AttemptWindow current = attempts.compute(key, (k, previous) -> {
            if (previous == null || now - previous.since() >= WINDOW_MILLIS) {
                return new AttemptWindow(now, 1);
            }
            return new AttemptWindow(previous.since(), previous.count() + 1);
        });
        if (current.count() > MAX_PER_MINUTE) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    private void enforceContactRateLimit(String whatsapp) {
        // Sólo cifras; el mismo número con espacios, guiones o + comparte ventana.
        String key = whatsapp.replaceAll("[^0-9]", "");
        if (key.length() < 8 || key.length() > 15) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "WhatsApp inválido");
        }

        long now = System.currentTimeMillis();
        // Evita crecimiento indefinido ante números distintos automatizados.
        if (contactAttempts.size() >= MAX_CONTACT_WINDOWS) {
            contactAttempts.entrySet().removeIf(entry ->
                    now - entry.getValue().since() >= CONTACT_WINDOW_MILLIS);
            if (!contactAttempts.containsKey(key)
                    && contactAttempts.size() >= MAX_CONTACT_WINDOWS) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
            }
        }
        AttemptWindow current = contactAttempts.compute(key, (k, previous) -> {
            if (previous == null
                    || now - previous.since() >= CONTACT_WINDOW_MILLIS) {
                return new AttemptWindow(now, 1);
            }
            return new AttemptWindow(previous.since(), previous.count() + 1);
        });
        if (current.count() > MAX_PER_CONTACT_WINDOW) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    private static String trim(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
