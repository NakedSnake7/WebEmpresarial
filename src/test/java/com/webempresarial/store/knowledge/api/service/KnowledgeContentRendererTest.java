package com.webempresarial.store.knowledge.api.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeContentRendererTest {

    private final KnowledgeContentRenderer renderer =
            new KnowledgeContentRenderer();

    @Test
    void shouldRenderMarkdownAsSafeHtml() {
        KnowledgeContentRenderer.RenderedKnowledgeContent result =
                renderer.render(
                        """
                        # Knowledge Engine

                        - Query Engine
                        - Version Engine

                        **Contenido importante**
                        """,
                        "MARKDOWN"
                );

        assertThat(result.format())
                .isEqualTo("MARKDOWN");

        assertThat(result.html())
                .contains("<h1>Knowledge Engine</h1>")
                .contains("<li>Query Engine</li>")
                .contains("<strong>Contenido importante</strong>");
    }

    @Test
    void shouldRejectDangerousEmbeddedHtmlInMarkdown() {

        KnowledgeContentRenderer.RenderedKnowledgeContent result =
                renderer.render(
                        """
                        # Safe content

                        <script>alert('xss')</script>
                        """,
                        "MARKDOWN"
                );

        assertThat(result.format())
                .isEqualTo("MARKDOWN");

        assertThat(result.html())
                .contains("<h1>Safe content</h1>")
                .doesNotContain("<script>")
                .doesNotContain("</script>")
                .contains("&lt;script&gt;")
                .contains("&lt;/script&gt;");
        assertThat(result.html())
        .doesNotContainIgnoringCase(
                "javascript:"
        )
        .doesNotContainIgnoringCase(
                "onerror="
        )
        .doesNotContainIgnoringCase(
                "onclick="
        );
    }

    @Test
    void shouldSanitizeHtmlContent() {
        KnowledgeContentRenderer.RenderedKnowledgeContent result =
                renderer.render(
                        """
                        <h2>Contenido</h2>
                        <img src="x" onerror="alert('xss')">
                        """,
                        "HTML"
                );

        assertThat(result.html())
                .contains("<h2>Contenido</h2>")
                .doesNotContain("onerror")
                .doesNotContain("alert");
    }

    @Test
    void shouldRenderPlainTextSafely() {
        KnowledgeContentRenderer.RenderedKnowledgeContent result =
                renderer.render(
                        "<script>alert('xss')</script>",
                        "PLAIN_TEXT"
                );

        assertThat(result.html())
                .contains("&lt;script&gt;")
                .doesNotContain("<script>");
    }
}