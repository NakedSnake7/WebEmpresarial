package com.webempresarial.store.digitaltransformation.domain.source;

import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SourceDocumentContentTest {

    @Test
    void shouldAdvanceThroughExtractionLifecycle() {
        TransformationSourceDocument source = TestSources.validSource();

        SourceDocumentContent content =
                SourceDocumentContent.create(
                        source,
                        1,
                        SourceContentExtractionMethod.NATIVE_PDF_TEXT,
                        "Apache PDFBox",
                        "3.0"
                );

        content.startExtraction();

        content.completeExtraction(
                "La marca de Robert es más fuerte " +
                "que su plataforma digital.",
                "es"
        );

        content.verify();
        content.markCurrent();

        assertThat(content.getExtractionStatus())
                .isEqualTo(
                        SourceContentExtractionStatus.VERIFIED
                );

        assertThat(content.isCurrent()).isTrue();
        assertThat(content.getCharacterCount()).isPositive();
        assertThat(content.getWordCount()).isEqualTo(11);
        assertThat(content.getDetectedLanguageCode())
                .isEqualTo("es");
    }

    @Test
    void shouldRejectCompletionBeforeExtractionStarts() {
        SourceDocumentContent content =
                SourceDocumentContent.create(
                        TestSources.validSource(),
                        1,
                        SourceContentExtractionMethod.NATIVE_PDF_TEXT,
                        null,
                        null
                );

        assertThatThrownBy(() ->
                content.completeExtraction(
                        "Contenido",
                        "es"
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EXTRACTING");
    }

    @Test
    void shouldRejectCurrentWhenNotVerified() {
        SourceDocumentContent content =
                SourceDocumentContent.create(
                        TestSources.validSource(),
                        1,
                        SourceContentExtractionMethod.NATIVE_PDF_TEXT,
                        null,
                        null
                );

        assertThatThrownBy(content::markCurrent)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("verificada");
    }
}