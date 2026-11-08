package com.webempresarial.store.repository;

import static org.assertj.core.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.entity.PilotOffer;
import com.webempresarial.store.model.PilotAcceptanceChannel;
import com.webempresarial.store.model.PilotCohort;
import com.webempresarial.store.model.PilotOfferStatus;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.model.ThemeType;
import com.webempresarial.store.service.PilotOfferService;

import jakarta.persistence.EntityManager;

@DataJpaTest(properties = {
    "spring.datasource.url=jdbc:mysql://127.0.0.1:3306/webempresarial_flyway_test",
    "spring.flyway.enabled=true",
    "spring.flyway.locations=classpath:db/migration",
    "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
@ActiveProfiles("dev")
@Import(PilotOfferService.class)
class PilotOfferPersistenceIntegrationTest {

    private static final String TEST_DATABASE =
            "webempresarial_flyway_test";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private PilotOfferRepository offerRepository;

    @Autowired
    private PilotOfferService pilotOfferService;

    @BeforeEach
    void verifyDatabaseIsolation() throws SQLException {
        try (Connection connection =
                dataSource.getConnection()) {

            assertThat(connection.getCatalog())
                    .as("No se permite usar la base dev")
                    .isEqualTo(TEST_DATABASE);
        }
    }

    private Store createStore(String label) {
        Store store = new Store();

        String suffix = UUID.randomUUID()
                .toString()
                .replace("-", "");

        store.setNombre("Pilot IT " + label);
        store.setDominio(
                "pilot-it-" + suffix + ".test"
        );
        store.setTheme("WebEmpresarial");
        store.setThemeType(ThemeType.BASIC);
        store.setPlan(StorePlan.BASIC);
        store.setActiva(true);

        return storeRepository.saveAndFlush(store);
    }

    private Lead createLead(Store store) {
        Lead lead = new Lead();

        lead.setStore(store);
        lead.setNombre("Pilot Test Lead");
        lead.setWhatsapp("5500000000");
        lead.setServicio("ECOMMERCE");
        lead.setPresupuesto("NO_DEFINIDO");

        return leadRepository.saveAndFlush(lead);
    }

    @Test
    void persistsOfferAndEnforcesStoreScopedQueries() {
        Store storeA = createStore("A");
        Store storeB = createStore("B");
        Lead leadA = createLead(storeA);

        PilotOffer offer = pilotOfferService.createDraft(
                storeA.getId(),
                leadA.getId(),
                PilotCohort.A
        );

        Long offerId = offer.getId();

        entityManager.flush();
        entityManager.clear();

        PilotOffer restored = offerRepository
                .findByIdAndStore_Id(
                        offerId,
                        storeA.getId()
                )
                .orElseThrow();

        assertThat(restored.getStatus())
                .isEqualTo(PilotOfferStatus.DRAFT);

        assertThat(restored.getInitialAmountMxn())
                .isEqualByComparingTo("990.00");

        assertThat(restored.getRenewalAmountMxn())
                .isEqualByComparingTo("790.00");

        assertThat(
                offerRepository.findByIdAndStore_Id(
                        offerId,
                        storeB.getId()
                )
        ).isEmpty();

        assertThat(
                offerRepository
                    .findByStore_IdOrderByCreatedAtDesc(
                        storeB.getId()
                    )
        ).noneMatch(item ->
                offerId.equals(item.getId())
        );

        entityManager.clear();

        // Comprueba que EntityGraph permite acceder
        // a las relaciones después del detach.
        assertThat(restored.getLead().getNombre())
                .isEqualTo("Pilot Test Lead");
    }

    @Test
    void rejectsLeadFromAnotherStore() {
        Store storeA = createStore("A");
        Store storeB = createStore("B");
        Lead leadA = createLead(storeA);

        assertThatThrownBy(() ->
                pilotOfferService.createDraft(
                        storeB.getId(),
                        leadA.getId(),
                        PilotCohort.B
                )
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    void persistsAcceptanceAndVersionedUpdates() {
        Store store = createStore("Acceptance");
        Lead lead = createLead(store);

        PilotOffer draft = pilotOfferService.createDraft(
                store.getId(),
                lead.getId(),
                PilotCohort.B
        );

        Long offerId = draft.getId();

        entityManager.flush();
        entityManager.clear();

        pilotOfferService.publish(
                store.getId(),
                offerId,
                LocalDateTime.now().plusDays(7)
        );

        entityManager.flush();
        entityManager.clear();

        pilotOfferService.recordAcceptance(
                store.getId(),
                offerId,
                PilotAcceptanceChannel.EMAIL,
                "email-test-reference-123",
                "root@platform.test"
        );

        entityManager.flush();
        entityManager.clear();

        PilotOffer restored = offerRepository
                .findByIdAndStore_Id(
                        offerId,
                        store.getId()
                )
                .orElseThrow();

        assertThat(restored.getStatus())
                .isEqualTo(PilotOfferStatus.ACCEPTED);

        assertThat(restored.getAcceptanceChannel())
                .isEqualTo(PilotAcceptanceChannel.EMAIL);

        assertThat(restored.getAcceptanceReference())
                .isEqualTo("email-test-reference-123");

        assertThat(restored.getAcceptanceRecordedBy())
                .isEqualTo("root@platform.test");

        assertThat(restored.getAcceptedTermsVersion())
                .isEqualTo("pricing-v0.1");

        assertThat(restored.getAcceptedInitialAmountMxn())
                .isEqualByComparingTo("1490.00");

        assertThat(restored.getAcceptedRenewalAmountMxn())
                .isEqualByComparingTo("790.00");

        assertThat(restored.getAcceptedAt())
                .isNotNull();

        Number version = (Number) entityManager
                .createNativeQuery(
                    "SELECT version FROM pilot_offers WHERE id = :id"
                )
                .setParameter("id", offerId)
                .getSingleResult();

        assertThat(version.longValue())
                .isGreaterThanOrEqualTo(2L);
    }

    @Test
    void mysqlRejectsCrossTenantOfferAssociation() {
        Store storeA = createStore("FK-A");
        Store storeB = createStore("FK-B");

        Lead leadA = createLead(storeA);

        PilotOffer offer = pilotOfferService.createDraft(
                storeA.getId(),
                leadA.getId(),
                PilotCohort.A
        );

        entityManager.flush();

        // storeB existe, pero leadA pertenece a storeA.
        // Las FK individuales serían válidas; la
        // FK compuesta debe rechazar la asociación.
        assertThatThrownBy(() ->
                entityManager.createNativeQuery(
                    "UPDATE pilot_offers "
                    + "SET store_id = :otherStore "
                    + "WHERE id = :offerId"
                )
                .setParameter(
                    "otherStore", storeB.getId()
                )
                .setParameter(
                    "offerId", offer.getId()
                )
                .executeUpdate()
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    void persistsTransferPaymentAndThirtyDayActivation() {
        Store store = createStore("Payment");
        Lead lead = createLead(store);

        PilotOffer offer = pilotOfferService.createDraft(
                store.getId(), lead.getId(), PilotCohort.B
        );

        Long offerId = offer.getId();

        entityManager.flush();
        entityManager.clear();

        pilotOfferService.publish(
                store.getId(),
                offerId,
                LocalDateTime.now().plusDays(7)
        );

        pilotOfferService.recordAcceptance(
                store.getId(),
                offerId,
                PilotAcceptanceChannel.EMAIL,
                "acceptance-payment-it",
                "root@platform.test"
        );

        pilotOfferService.beginTransferPayment(
                store.getId(), offerId
        );

        pilotOfferService.confirmTransferPayment(
                store.getId(),
                offerId,
                "BANK:TEST:TX-987654",
                "root@platform.test"
        );

        entityManager.flush();
        entityManager.clear();

        PilotOffer paid = offerRepository
                .findByIdAndStore_Id(
                        offerId, store.getId()
                )
                .orElseThrow();

        assertThat(paid.getStatus())
                .isEqualTo(PilotOfferStatus.PAID);

        assertThat(paid.getPaymentEvidenceReference())
                .isEqualTo("BANK:TEST:TX-987654");

        assertThat(paid.getPaymentVerifiedAt())
                .isNotNull();

        pilotOfferService.activatePilot(
                store.getId(), offerId
        );

        entityManager.flush();
        entityManager.clear();

        PilotOffer active = offerRepository
                .findByIdAndStore_Id(
                        offerId, store.getId()
                )
                .orElseThrow();

        assertThat(active.getStatus())
                .isEqualTo(PilotOfferStatus.ACTIVE);

        assertThat(active.getPilotEndsAt())
                .isEqualTo(
                        active.getPilotStartedAt().plusDays(30)
                );

        assertThat(active.getPilotStartedAt())
                .isAfterOrEqualTo(active.getPaidAt());
    }

    @Test
    void mysqlRejectsDuplicateTransferReferenceAcrossOffers() {
        Store storeA = createStore("DuplicatePaymentA");
        Store storeB = createStore("DuplicatePaymentB");

        Lead leadA = createLead(storeA);
        Lead leadB = createLead(storeB);

        PilotOffer offerA = pilotOfferService.createDraft(
                storeA.getId(), leadA.getId(), PilotCohort.A
        );

        PilotOffer offerB = pilotOfferService.createDraft(
                storeB.getId(), leadB.getId(), PilotCohort.B
        );

        for (PilotOffer offer :
                new PilotOffer[] {offerA, offerB}) {

            Long storeId = offer.getStore().getId();

            pilotOfferService.publish(
                    storeId,
                    offer.getId(),
                    LocalDateTime.now().plusDays(7)
            );

            pilotOfferService.recordAcceptance(
                    storeId,
                    offer.getId(),
                    PilotAcceptanceChannel.EMAIL,
                    "acceptance-duplicate-" + offer.getId(),
                    "root@platform.test"
            );

            pilotOfferService.beginTransferPayment(
                    storeId, offer.getId()
            );
        }

        String bankReference =
                "BANK:TEST:DUPLICATE-" + UUID.randomUUID();

        pilotOfferService.confirmTransferPayment(
                storeA.getId(),
                offerA.getId(),
                bankReference,
                "root@platform.test"
        );

        // La primera referencia debe quedar registrada
        // en MySQL antes de intentar reutilizarla.
        entityManager.flush();

        pilotOfferService.confirmTransferPayment(
                storeB.getId(),
                offerB.getId(),
                bankReference,
                "root@platform.test"
        );

        // Exigimos que la excepción se origine
        // en el índice único concreto de V35.
        assertThatThrownBy(entityManager::flush)
                .isInstanceOf(RuntimeException.class)
                .hasStackTraceContaining(
                    "uk_pilot_offers_payment_evidence"
                );
    }

    @Test
    void mysqlRejectsPaidTransferWithoutReconciliation() {
        Store store = createStore("Unverified");
        Lead lead = createLead(store);

        PilotOffer offer = pilotOfferService.createDraft(
                store.getId(), lead.getId(), PilotCohort.A
        );

        pilotOfferService.publish(
                store.getId(),
                offer.getId(),
                LocalDateTime.now().plusDays(7)
        );

        pilotOfferService.recordAcceptance(
                store.getId(),
                offer.getId(),
                PilotAcceptanceChannel.EMAIL,
                "acceptance-unverified-it",
                "root@platform.test"
        );

        entityManager.flush();

        assertThatThrownBy(() ->
                entityManager.createNativeQuery(
                    "UPDATE pilot_offers "
                    + "SET status = 'PAID', "
                    + "payment_method = 'TRANSFER', "
                    + "paid_at = NOW(6) "
                    + "WHERE id = :id"
                )
                .setParameter("id", offer.getId())
                .executeUpdate()
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    void mysqlRejectsTamperedPilotPrice() {
        Store store = createStore("Price");
        Lead lead = createLead(store);

        PilotOffer offer = pilotOfferService.createDraft(
                store.getId(),
                lead.getId(),
                PilotCohort.A
        );

        entityManager.flush();

        assertThatThrownBy(() ->
                entityManager.createNativeQuery(
                    "UPDATE pilot_offers "
                    + "SET initial_amount_mxn = 123.00 "
                    + "WHERE id = :id"
                )
                .setParameter("id", offer.getId())
                .executeUpdate()
        ).isInstanceOf(RuntimeException.class);
    }
}
