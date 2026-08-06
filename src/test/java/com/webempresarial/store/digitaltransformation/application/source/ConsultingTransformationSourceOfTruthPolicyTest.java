package com.webempresarial.store.digitaltransformation.application.source;

import com.webempresarial.store.digitaltransformation.application.shared.IncompleteSourceOfTruthException;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProjectType;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocument;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceRole;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceType;
import com.webempresarial.store.model.Store;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultingTransformationSourceOfTruthPolicyTest {

    private final ConsultingTransformationSourceOfTruthPolicy policy =
            new ConsultingTransformationSourceOfTruthPolicy();

    @Test
    void shouldAcceptVerifiedAuditAndProposal() {
        TransformationProject project = validProject();

        TransformationSourceDocument audit =
                verifiedSource(
                        project,
                        TransformationSourceType
                                .DIGITAL_EXCELLENCE_AUDIT,
                        "a"
                );

        TransformationSourceDocument proposal =
                verifiedSource(
                        project,
                        TransformationSourceType
                                .DIGITAL_TRANSFORMATION_PROPOSAL,
                        "b"
                );

        assertThatCode(() ->
                policy.validate(
                        project,
                        List.of(audit, proposal)
                )
        ).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectWhenProposalIsMissing() {
        TransformationProject project = validProject();

        TransformationSourceDocument audit =
                verifiedSource(
                        project,
                        TransformationSourceType
                                .DIGITAL_EXCELLENCE_AUDIT,
                        "a"
                );

        assertThatThrownBy(() ->
                policy.validate(project, List.of(audit))
        )
                .isInstanceOf(
                        IncompleteSourceOfTruthException.class
                )
                .hasMessageContaining(
                        "DIGITAL_TRANSFORMATION_PROPOSAL"
                );
    }

    @Test
    void shouldRejectUnverifiedSource() {
        TransformationProject project = validProject();

        TransformationSourceDocument audit =
                registeredSource(
                        project,
                        TransformationSourceType
                                .DIGITAL_EXCELLENCE_AUDIT,
                        "a"
                );

        TransformationSourceDocument proposal =
                verifiedSource(
                        project,
                        TransformationSourceType
                                .DIGITAL_TRANSFORMATION_PROPOSAL,
                        "b"
                );

        assertThatThrownBy(() ->
                policy.validate(
                        project,
                        List.of(audit, proposal)
                )
        )
                .isInstanceOf(
                        IncompleteSourceOfTruthException.class
                )
                .hasMessageContaining("verificadas");
    }

    private static TransformationSourceDocument verifiedSource(
            TransformationProject project,
            TransformationSourceType type,
            String checksumCharacter
    ) {
        TransformationSourceDocument source =
                registeredSource(
                        project,
                        type,
                        checksumCharacter
                );

        source.markUploaded();
        source.markParsed();
        source.markAnalyzed();
        source.verify();

        return source;
    }

    private static TransformationSourceDocument registeredSource(
            TransformationProject project,
            TransformationSourceType type,
            String checksumCharacter
    ) {
        String checksum = checksumCharacter.repeat(64);

        return TransformationSourceDocument.register(
                project,
                type,
                TransformationSourceRole.SOURCE_OF_TRUTH,
                type.name() + ".pdf",
                type.name(),
                "application/pdf",
                "storage://" + type.name(),
                checksum,
                1,
                "es",
                20
        );
    }

    private static TransformationProject validProject() {
        Store store = mock(Store.class);
        when(store.getId()).thenReturn(1L);

        return TransformationProject.create(
                store,
                "DTE-001",
                "Proyecto",
                "Robert Slingerland",
                "https://robertslingerland.com",
                TransformationProjectType.BRAND_EXPERIENCE,
                "Transformación digital"
        );
    }
}