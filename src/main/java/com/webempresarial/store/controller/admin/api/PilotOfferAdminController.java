package com.webempresarial.store.controller.admin.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.webempresarial.store.entity.PilotOffer;
import com.webempresarial.store.model.PilotAcceptanceChannel;
import com.webempresarial.store.model.PilotCohort;
import com.webempresarial.store.model.PilotOfferStatus;
import com.webempresarial.store.model.PilotPaymentMethod;
import org.springframework.dao.DataIntegrityViolationException;
import com.webempresarial.store.service.PilotOfferService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@RestController
@RequestMapping("/api/admin/platform/pilot-offers")
public class PilotOfferAdminController {

    private final PilotOfferService service;

    public PilotOfferAdminController(
            PilotOfferService service
    ) {
        this.service = service;
    }

    public record DraftRequest(
            @NotNull @Positive Long leadId,
            @NotNull PilotCohort cohort
    ) {}

    public record PublishRequest(
            @NotNull @Min(1) @Max(30) Integer validityDays
    ) {}

    public record AcceptanceRequest(
            @NotNull PilotAcceptanceChannel channel,
            @NotBlank @Size(max = 255) String evidenceReference
    ) {}

    public record PilotOfferView(
            Long id,
            Long storeId,
            Long leadId,
            PilotCohort cohort,
            PilotOfferStatus status,
            BigDecimal initialAmountMxn,
            BigDecimal renewalAmountMxn,
            String currency,
            String termsVersion,
            LocalDateTime offeredAt,
            LocalDateTime expiresAt,
            LocalDateTime acceptedAt,
            PilotAcceptanceChannel acceptanceChannel,
            String acceptanceReference,
            String acceptanceRecordedBy,
            PilotPaymentMethod paymentMethod,
            LocalDateTime paidAt,
            String paymentEvidenceReference,
            String paymentVerifiedBy,
            LocalDateTime paymentVerifiedAt,
            LocalDateTime pilotStartedAt,
            LocalDateTime pilotEndsAt
    ) {
        public static PilotOfferView from(PilotOffer offer) {
            return new PilotOfferView(
                    offer.getId(),
                    offer.getStore().getId(),
                    offer.getLead().getId(),
                    offer.getCohort(),
                    offer.getStatus(),
                    offer.getInitialAmountMxn(),
                    offer.getRenewalAmountMxn(),
                    offer.getCurrency(),
                    offer.getTermsVersion(),
                    offer.getOfferedAt(),
                    offer.getExpiresAt(),
                    offer.getAcceptedAt(),
                    offer.getAcceptanceChannel(),
                    offer.getAcceptanceReference(),
                    offer.getAcceptanceRecordedBy(),
                    offer.getPaymentMethod(),
                    offer.getPaidAt(),
                    offer.getPaymentEvidenceReference(),
                    offer.getPaymentVerifiedBy(),
                    offer.getPaymentVerifiedAt(),
                    offer.getPilotStartedAt(),
                    offer.getPilotEndsAt()
            );
        }
    }


    /**
     * No revela si una oferta existe bajo otro tenant.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> handleOfferNotFound(
            EntityNotFoundException exception
    ) {
        return ResponseEntity.notFound().build();
    }


    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Void> handleInvalidTransition(
            IllegalStateException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Void> handleDuplicatePaymentReference(
            DataIntegrityViolationException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleInvalidPilotArgument(
            IllegalArgumentException exception
    ) {
        return ResponseEntity.badRequest().build();
    }

    private static String requireSuperAdmin(
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getAuthorities().stream()
                    .noneMatch(authority ->
                        "ROLE_SUPER_ADMIN".equals(
                            authority.getAuthority()
                        )
                    )) {
            throw new AccessDeniedException(
                    "Se requiere SUPER_ADMIN"
            );
        }

        String identity = authentication.getName();

        if (identity == null || identity.isBlank()) {
            throw new AccessDeniedException(
                    "Administrador sin identidad válida"
            );
        }

        return identity;
    }

    @PostMapping("/stores/{storeId}")
    public ResponseEntity<PilotOfferView> createDraft(
            @PathVariable Long storeId,
            @Valid @RequestBody DraftRequest request,
            Authentication authentication
    ) {
        requireSuperAdmin(authentication);

        PilotOffer offer = service.createDraft(
                storeId,
                request.leadId(),
                request.cohort()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PilotOfferView.from(offer));
    }

    @GetMapping("/stores/{storeId}")
    public List<PilotOfferView> list(
            @PathVariable Long storeId,
            Authentication authentication
    ) {
        requireSuperAdmin(authentication);

        return service.listForStore(storeId)
                .stream()
                .map(PilotOfferView::from)
                .toList();
    }

    @GetMapping("/stores/{storeId}/{offerId}")
    public PilotOfferView get(
            @PathVariable Long storeId,
            @PathVariable Long offerId,
            Authentication authentication
    ) {
        requireSuperAdmin(authentication);

        return PilotOfferView.from(
                service.getForStore(storeId, offerId)
        );
    }

    @PostMapping("/stores/{storeId}/{offerId}/publish")
    public PilotOfferView publish(
            @PathVariable Long storeId,
            @PathVariable Long offerId,
            @Valid @RequestBody PublishRequest request,
            Authentication authentication
    ) {
        requireSuperAdmin(authentication);

        if (request.validityDays() == null
                || request.validityDays() < 1
                || request.validityDays() > 30) {
            throw new IllegalArgumentException(
                    "La vigencia debe ser entre 1 y 30 días"
            );
        }

        return PilotOfferView.from(
                service.publish(
                        storeId,
                        offerId,
                        LocalDateTime.now()
                                .plusDays(request.validityDays())
                )
        );
    }

    @PostMapping("/stores/{storeId}/{offerId}/accept")
    public PilotOfferView accept(
            @PathVariable Long storeId,
            @PathVariable Long offerId,
            @Valid @RequestBody AcceptanceRequest request,
            Authentication authentication
    ) {
        String recordedBy = requireSuperAdmin(authentication);

        return PilotOfferView.from(
                service.recordAcceptance(
                        storeId,
                        offerId,
                        request.channel(),
                        request.evidenceReference(),
                        recordedBy
                )
        );
    }

    public record ConfirmTransferRequest(
            @NotBlank @Size(max = 255)
            String bankTransactionReference
    ) {}

    @PostMapping("/stores/{storeId}/{offerId}/transfer/start")
    public PilotOfferView startTransfer(
            @PathVariable Long storeId,
            @PathVariable Long offerId,
            Authentication authentication
    ) {
        requireSuperAdmin(authentication);

        return PilotOfferView.from(
                service.beginTransferPayment(storeId, offerId)
        );
    }

    /**
     * Operación exclusivamente administrativa.
     * Se debe conciliar el movimiento bancario antes de llamarla.
     */
    @PostMapping("/stores/{storeId}/{offerId}/transfer/confirm")
    public PilotOfferView confirmTransfer(
            @PathVariable Long storeId,
            @PathVariable Long offerId,
            @Valid @RequestBody ConfirmTransferRequest request,
            Authentication authentication
    ) {
        String verifiedBy = requireSuperAdmin(authentication);

        return PilotOfferView.from(
                service.confirmTransferPayment(
                        storeId,
                        offerId,
                        request.bankTransactionReference(),
                        verifiedBy
                )
        );
    }

    @PostMapping("/stores/{storeId}/{offerId}/activate")
    public PilotOfferView activate(
            @PathVariable Long storeId,
            @PathVariable Long offerId,
            Authentication authentication
    ) {
        requireSuperAdmin(authentication);

        return PilotOfferView.from(
                service.activatePilot(storeId, offerId)
        );
    }
}
