package com.webempresarial.store.knowledge.api.service;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class KnowledgeContentRenderer {

    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;
    private final Safelist knowledgeSafelist;

    public KnowledgeContentRenderer() {
        this.markdownParser =
                Parser.builder()
                        .build();

        /*
         * escapeHtml evita que HTML embebido en Markdown
         * sea interpretado directamente por el renderer.
         */
        this.htmlRenderer =
                HtmlRenderer.builder()
                        .escapeHtml(true)
                        .build();

        this.knowledgeSafelist =
                Safelist.relaxed()
                        .addTags(
                                "pre",
                                "code",
                                "blockquote",
                                "hr"
                        )
                        .addAttributes(
                                "a",
                                "target",
                                "rel"
                        )
                        .addAttributes(
                                "code",
                                "class"
                        )
                        .addProtocols(
                                "a",
                                "href",
                                "http",
                                "https",
                                "mailto"
                        );
    }

    public RenderedKnowledgeContent render(
            String content,
            String contentFormat
    ) {
        if (content == null || content.isBlank()) {
            return new RenderedKnowledgeContent(
                    "",
                    normalizeFormat(contentFormat)
            );
        }

        String normalizedFormat =
                normalizeFormat(contentFormat);

        return switch (normalizedFormat) {
            case "MARKDOWN" ->
                    new RenderedKnowledgeContent(
                            renderMarkdown(content),
                            normalizedFormat
                    );

            case "HTML" ->
                    new RenderedKnowledgeContent(
                            sanitizeHtml(content),
                            normalizedFormat
                    );

            default ->
                    new RenderedKnowledgeContent(
                            renderPlainText(content),
                            "PLAIN_TEXT"
                    );
        };
    }

    private String renderMarkdown(
            String markdown
    ) {
        Node document =
                markdownParser.parse(markdown);

        String renderedHtml =
                htmlRenderer.render(document);

        return sanitizeHtml(renderedHtml);
    }

    private String renderPlainText(
            String text
    ) {
        /*
         * Jsoup escapa caracteres especiales antes de
         * envolver el contenido en <pre>.
         */
        String escaped =
                org.jsoup.nodes.Entities.escape(text);

        return "<pre>" + escaped + "</pre>";
    }

    private String sanitizeHtml(
            String html
    ) {
        return Jsoup.clean(
                html,
                knowledgeSafelist
        );
    }

    private String normalizeFormat(
            String contentFormat
    ) {
        if (contentFormat == null
                || contentFormat.isBlank()) {

            return "PLAIN_TEXT";
        }

        return contentFormat
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    public record RenderedKnowledgeContent(
            String html,
            String format
    ) {
    }
}