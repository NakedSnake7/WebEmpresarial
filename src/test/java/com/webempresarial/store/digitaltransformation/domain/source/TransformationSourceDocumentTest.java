package com.webempresarial.store.digitaltransformation.domain.source;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProjectType;
import com.webempresarial.store.model.Store;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransformationSourceDocumentTest {

    private static final String CHECKSUM =
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    @Test
    void shouldRegisterSourceOfTruthAsAuthoritative() {
        TransformationSourceDocument document =
                validSource(
                        TransformationSourceRole.SOURCE_OF_TRUTH
                );

        assertThat(document.getStatus())
                .isEqualTo(
                        TransformationSourceStatus.REGISTERED
                );

        assertThat(document.isAuthoritative()).isTrue();
    }

    @Test
    void shouldAdvanceThroughDocumentLifecycle() {
        TransformationSourceDocument document =
                validSource(
                        TransformationSourceRole.SOURCE_OF_TRUTH
                );

        document.markUploaded();
        document.markParsed();
        document.markAnalyzed();
        document.verify();

        assertThat(document.getStatus())
                .isEqualTo(
                        TransformationSourceStatus.VERIFIED
                );

        assertThat(document.isVerifiedAuthoritativeSource())
                .isTrue();
    }

    @Test
    void shouldRejectAnalysisBeforeParsing() {
        TransformationSourceDocument document =
                validSource(
                        TransformationSourceRole.SOURCE_OF_TRUTH
                );

        assertThatThrownBy(document::markAnalyzed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PARSED");
    }

    @Test
    void shouldRejectInvalidChecksum() {
        TransformationProject project = validProject();

        assertThatThrownBy(() ->
                TransformationSourceDocument.register(
                        project,
                        TransformationSourceType
                                .DIGITAL_EXCELLENCE_AUDIT,
                        TransformationSourceRole.SOURCE_OF_TRUTH,
                        "audit.pdf",
                        "Digital Excellence Audit",
                        "application/pdf",
                        "cloudinary://audit.pdf",
                        "checksum-invalido",
                        1,
                        "es",
                        20
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SHA-256");
    }

    @Test
    void shouldRejectInvalidPageCount() {
        TransformationProject project = validProject();

        assertThatThrownBy(() ->
                TransformationSourceDocument.register(
                        project,
                        TransformationSourceType
                                .DIGITAL_EXCELLENCE_AUDIT,
                        TransformationSourceRole.SOURCE_OF_TRUTH,
                        "audit.pdf",
                        "Digital Excellence Audit",
                        "application/pdf",
                        "cloudinary://audit.pdf",
                        CHECKSUM,
                        1,
                        "es",
                        0
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("páginas");
    }

    private static TransformationSourceDocument validSource(
            TransformationSourceRole role
    ) {
        return TransformationSourceDocument.register(
                validProject(),
                TransformationSourceType.DIGITAL_EXCELLENCE_AUDIT,
                role,
                "Digital_Excellence_Audit_ES.pdf",
                "Digital Excellence Audit",
                "application/pdf",
                "cloudinary://digital-transformation/audit.pdf",
                CHECKSUM,
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