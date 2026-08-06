package com.webempresarial.store.digitaltransformation.support;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProjectType;
import com.webempresarial.store.digitaltransformation.domain.source.SourceContentExtractionMethod;
import com.webempresarial.store.digitaltransformation.domain.source.SourceDocumentContent;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocument;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceRole;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceType;
import com.webempresarial.store.model.Store;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TestSources {

    private static final String CHECKSUM =
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    private TestSources() {
    }

    public static TransformationProject validProject() {
        Store store = mock(Store.class);
        when(store.getId()).thenReturn(1L);

        return TransformationProject.create(
                store,
                "DTE-001",
                "Proyecto de transformación",
                "Robert Slingerland",
                "https://robertslingerland.com",
                TransformationProjectType.BRAND_EXPERIENCE,
                "Crear una experiencia digital premium"
        );
    }

    public static TransformationSourceDocument validSource() {
        TransformationSourceDocument source =
                TransformationSourceDocument.register(
                        validProject(),
                        TransformationSourceType
                                .DIGITAL_EXCELLENCE_AUDIT,
                        TransformationSourceRole.SOURCE_OF_TRUTH,
                        "Digital_Excellence_Audit_ES.pdf",
                        "Digital Excellence Audit",
                        "application/pdf",
                        "storage://audit.pdf",
                        CHECKSUM,
                        1,
                        "es",
                        20
                );

        source.markUploaded();
        source.markParsed();
        source.markAnalyzed();
        source.verify();

        return source;
    }

    public static SourceDocumentContent verifiedContent() {
        SourceDocumentContent content =
                SourceDocumentContent.create(
                        validSource(),
                        1,
                        SourceContentExtractionMethod.NATIVE_PDF_TEXT,
                        "PDFBox",
                        "3.0"
                );

        content.startExtraction();
        content.completeExtraction(
                "Contenido estratégico verificado del documento.",
                "es"
        );
        content.verify();
        content.markCurrent();

        return content;
    }
}