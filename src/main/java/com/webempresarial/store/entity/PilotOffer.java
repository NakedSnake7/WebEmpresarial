package com.webempresarial.store.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import com.webempresarial.store.model.PilotAcceptanceChannel;
import com.webempresarial.store.model.PilotCohort;
import com.webempresarial.store.model.PilotOfferStatus;
import com.webempresarial.store.model.PilotPaymentMethod;
import com.webempresarial.store.model.Store;

import jakarta.persistence.*;

@Entity
@Table(name = "pilot_offers")
public class PilotOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lead_id", nullable = false)
    private Lead lead;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1, updatable = false)
    private PilotCohort cohort;

    @Column(
        name = "initial_amount_mxn",
        nullable = false,
        precision = 12,
        scale = 2,
        updatable = false
    )
    private BigDecimal initialAmountMxn;

    @Column(
        name = "renewal_amount_mxn",
        nullable = false,
        precision = 12,
        scale = 2,
        updatable = false
    )
    private BigDecimal renewalAmountMxn;

    @Column(nullable = false, length = 3, updatable = false)
    private String currency = "MXN";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private PilotOfferStatus status = PilotOfferStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 16)
    private PilotPaymentMethod paymentMethod;

    @Column(name = "terms_version", nullable = false, length = 32)
    private String termsVersion = "pricing-v0.1";

    @Column(name = "stripe_checkout_session_id", length = 255)
    private String stripeCheckoutSessionId;

    @Column(name = "stripe_payment_intent_id", length = 255)
    private String stripePaymentIntentId;

    @Column(name = "offered_at")
    private LocalDateTime offeredAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "acceptance_channel", length = 32)
    private PilotAcceptanceChannel acceptanceChannel;

    @Column(name = "acceptance_reference", length = 255)
    private String acceptanceReference;

    @Column(name = "acceptance_recorded_by", length = 120)
    private String acceptanceRecordedBy;

    @Column(name = "accepted_terms_version", length = 32)
    private String acceptedTermsVersion;

    @Column(name = "accepted_initial_amount_mxn",
            precision = 12, scale = 2)
    private BigDecimal acceptedInitialAmountMxn;

    @Column(name = "accepted_renewal_amount_mxn",
            precision = 12, scale = 2)
    private BigDecimal acceptedRenewalAmountMxn;


    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "payment_evidence_reference", length = 255)
    private String paymentEvidenceReference;

    @Column(name = "payment_verified_by", length = 120)
    private String paymentVerifiedBy;

    @Column(name = "payment_verified_at")
    private LocalDateTime paymentVerifiedAt;

    @Column(name = "pilot_started_at")
    private LocalDateTime pilotStartedAt;

    @Column(name = "pilot_ends_at")
    private LocalDateTime pilotEndsAt;


    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected PilotOffer() {
        // JPA
    }

    public static PilotOffer draft(
            Store store,
            Lead lead,
            PilotCohort cohort
    ) {
        Objects.requireNonNull(store, "Store obligatorio");
        Objects.requireNonNull(lead, "Lead obligatorio");
        Objects.requireNonNull(cohort, "Cohorte obligatoria");

        if (store.getId() == null
                || lead.getId() == null
                || lead.getStore() == null
                || !Objects.equals(
                        store.getId(),
                        lead.getStore().getId()
                )) {
            throw new IllegalArgumentException(
                    "Lead y Store deben pertenecer al mismo tenant"
            );
        }

        PilotOffer offer = new PilotOffer();

        offer.store = store;
        offer.lead = lead;
        offer.cohort = cohort;
        offer.initialAmountMxn = cohort.initialAmountMxn();
        offer.renewalAmountMxn = PilotCohort.RENEWAL_AMOUNT_MXN;

        return offer;
    }


    /**
     * Publica una oferta con precio previamente fijado
     * por PilotCohort en el backend.
     */
    public void markOffered(
            LocalDateTime offeredAt,
            LocalDateTime expiresAt
    ) {
        Objects.requireNonNull(offeredAt, "Fecha obligatoria");
        Objects.requireNonNull(expiresAt, "Vigencia obligatoria");

        if (status != PilotOfferStatus.DRAFT) {
            throw new IllegalStateException(
                    "Solo se pueden publicar ofertas DRAFT"
            );
        }

        if (!expiresAt.isAfter(offeredAt)) {
            throw new IllegalArgumentException(
                    "La fecha de expiración debe ser posterior"
            );
        }

        this.status = PilotOfferStatus.OFFERED;
        this.offeredAt = offeredAt;
        this.expiresAt = expiresAt;
    }


    /**
     * Registra una aceptación documentada.
     * La evidencia debe comprobarse antes de invocar este método.
     */
    public void recordAcceptance(
            PilotAcceptanceChannel channel,
            String reference,
            String recordedBy,
            LocalDateTime now
    ) {
        Objects.requireNonNull(channel, "Canal obligatorio");
        Objects.requireNonNull(now, "Fecha obligatoria");

        if (status != PilotOfferStatus.OFFERED) {
            throw new IllegalStateException(
                    "Solo se aceptan ofertas OFFERED"
            );
        }

        if (offeredAt == null || expiresAt == null
                || now.isBefore(offeredAt)
                || !now.isBefore(expiresAt)) {
            throw new IllegalStateException(
                    "La oferta no está vigente"
            );
        }

        String validReference = requiredText(
                reference, 255, "Referencia de evidencia"
        );

        String validRecorder = requiredText(
                recordedBy, 120, "Responsable del registro"
        );

        this.acceptanceChannel = channel;
        this.acceptanceReference = validReference;
        this.acceptanceRecordedBy = validRecorder;
        this.acceptedTermsVersion = termsVersion;
        this.acceptedInitialAmountMxn = initialAmountMxn;
        this.acceptedRenewalAmountMxn = renewalAmountMxn;
        this.acceptedAt = now;
        this.status = PilotOfferStatus.ACCEPTED;
    }

    private static String requiredText(
            String value, int maxLength, String label
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    label + " es obligatorio"
            );
        }

        String normalized = value.trim();

        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(
                    label + " excede la longitud permitida"
            );
        }

        return normalized;
    }

    public PilotAcceptanceChannel getAcceptanceChannel() {
        return acceptanceChannel;
    }

    public String getAcceptanceReference() {
        return acceptanceReference;
    }

    public String getAcceptanceRecordedBy() {
        return acceptanceRecordedBy;
    }

    public String getAcceptedTermsVersion() {
        return acceptedTermsVersion;
    }

    public BigDecimal getAcceptedInitialAmountMxn() {
        return acceptedInitialAmountMxn;
    }

    public BigDecimal getAcceptedRenewalAmountMxn() {
        return acceptedRenewalAmountMxn;
    }


    /**
     * Inicia el flujo de transferencia después de la aceptación.
     * No representa confirmación de pago.
     */
    public void beginTransferPayment() {
        if (status == PilotOfferStatus.PAYMENT_PENDING
                && paymentMethod == PilotPaymentMethod.TRANSFER) {
            return;
        }

        if (status != PilotOfferStatus.ACCEPTED
                || acceptedAt == null) {
            throw new IllegalStateException(
                    "La oferta debe estar ACCEPTED"
            );
        }

        paymentMethod = PilotPaymentMethod.TRANSFER;
        status = PilotOfferStatus.PAYMENT_PENDING;
    }

    /**
     * Debe invocarse únicamente tras comprobar el movimiento
     * en el estado de cuenta del beneficiario.
     *
     * reference debe ser un identificador bancario inequívoco,
     * incluyendo banco y folio de movimiento.
     */
    public void confirmTransferPayment(
            String reference,
            String verifiedBy,
            LocalDateTime now
    ) {
        Objects.requireNonNull(now, "Fecha obligatoria");

        String validReference = requiredText(
                reference, 255, "Referencia bancaria"
        );

        String validVerifier = requiredText(
                verifiedBy, 120, "Verificador"
        );

        if (status == PilotOfferStatus.PAID
                || status == PilotOfferStatus.ACTIVE) {

            if (paymentMethod == PilotPaymentMethod.TRANSFER
                    && validReference.equals(
                            paymentEvidenceReference
                    )) {
                // Reintento de la misma confirmación.
                return;
            }

            throw new IllegalStateException(
                    "La oferta ya tiene otro pago registrado"
            );
        }

        if (status != PilotOfferStatus.PAYMENT_PENDING
                || paymentMethod != PilotPaymentMethod.TRANSFER
                || acceptedAt == null) {
            throw new IllegalStateException(
                    "No existe una transferencia pendiente"
            );
        }

        if (now.isBefore(acceptedAt)) {
            throw new IllegalArgumentException(
                    "La fecha de pago es anterior a la aceptación"
            );
        }

        paymentEvidenceReference = validReference;
        paymentVerifiedBy = validVerifier;
        paymentVerifiedAt = now;
        paidAt = now;
        status = PilotOfferStatus.PAID;
    }

    /**
     * El periodo de 30 días empieza durante la activación,
     * no durante el cobro.
     */
    public void activatePilot(LocalDateTime now) {
        Objects.requireNonNull(now, "Fecha obligatoria");

        if (status == PilotOfferStatus.ACTIVE) {
            // Activación repetida: no reinicia los 30 días.
            return;
        }

        if (status != PilotOfferStatus.PAID
                || paidAt == null) {
            throw new IllegalStateException(
                    "Se requiere un pago confirmado"
            );
        }

        if (now.isBefore(paidAt)) {
            throw new IllegalArgumentException(
                    "La activación no puede preceder al pago"
            );
        }

        pilotStartedAt = now;
        pilotEndsAt = now.plusDays(30);
        status = PilotOfferStatus.ACTIVE;
    }

    public String getPaymentEvidenceReference() {
        return paymentEvidenceReference;
    }

    public String getPaymentVerifiedBy() {
        return paymentVerifiedBy;
    }

    public LocalDateTime getPaymentVerifiedAt() {
        return paymentVerifiedAt;
    }

    public LocalDateTime getPilotStartedAt() {
        return pilotStartedAt;
    }

    public LocalDateTime getPilotEndsAt() {
        return pilotEndsAt;
    }

    @PrePersist
    void prePersist() {
        if (cohort == null
                || initialAmountMxn == null
                || renewalAmountMxn == null
                || !initialAmountMxn.equals(cohort.initialAmountMxn())
                || !renewalAmountMxn.equals(
                        PilotCohort.RENEWAL_AMOUNT_MXN
                )
                || !"MXN".equals(currency)
                || store == null
                || lead == null
                || lead.getStore() == null
                || !Objects.equals(
                        store.getId(),
                        lead.getStore().getId()
                )) {
            throw new IllegalStateException(
                    "Oferta de piloto inválida"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Store getStore() {
        return store;
    }

    public Lead getLead() {
        return lead;
    }

    public PilotCohort getCohort() {
        return cohort;
    }

    public BigDecimal getInitialAmountMxn() {
        return initialAmountMxn;
    }

    public BigDecimal getRenewalAmountMxn() {
        return renewalAmountMxn;
    }

    public String getCurrency() {
        return currency;
    }

    public PilotOfferStatus getStatus() {
        return status;
    }

    public PilotPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getTermsVersion() {
        return termsVersion;
    }

    public String getStripeCheckoutSessionId() {
        return stripeCheckoutSessionId;
    }

    public String getStripePaymentIntentId() {
        return stripePaymentIntentId;
    }

    public LocalDateTime getOfferedAt() {
        return offeredAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }
}
