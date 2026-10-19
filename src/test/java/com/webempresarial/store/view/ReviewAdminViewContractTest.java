package com.webempresarial.store.view;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewAdminViewContractTest {

    @Test
    void creationUiMustRequireReviewCreate()
            throws IOException {

        String html = reviews();

        assertThat(html)
                .contains(
                        """
                        <div class="col-xl-4" \
                        th:if="${canCreateReviews}">
                        """.strip()
                )
                .contains(
                        """
                        <form th:action="@{/resenas/nueva}"
                        """.strip()
                );
    }

    @Test
    void existingReviewsMustRequireReviewView()
            throws IOException {

        String html = reviews();

        assertThat(html)
                .contains(
                        """
                        th:if="${canViewReviews \
                        and resenas != null \
                        and !#lists.isEmpty(resenas)}"
                        """.strip()
                )
                .contains(
                        """
                        th:if="${canViewReviews \
                        and (resenas == null \
                        or #lists.isEmpty(resenas))}"
                        """.strip()
                );
    }

    @Test
    void editControlsMustRequireReviewEdit()
            throws IOException {

        String html = reviews();

        assertThat(html)
                .contains(
                        """
                        th:if="${canEditReviews}" \
                        th:id="'edit-panel-' + ${review.id}"
                        """.strip()
                )
                .contains(
                        """
                        th:if="${canEditReviews}" \
                        class="btn btn-sm \
                        btn-outline-warning \
                        edit-review"
                        """.strip()
                )
                .contains(
                        """
                        th:if="${canEditReviews}" \
                        class="btn btn-sm \
                        btn-outline-secondary \
                        delete-image"
                        """.strip()
                );
    }

    @Test
    void deleteControlMustRequireReviewDelete()
            throws IOException {

        String html = reviews();

        assertThat(html)
                .contains(
                        """
                        th:if="${canDeleteReviews}" \
                        class="btn btn-sm \
                        btn-outline-danger \
                        delete-review"
                        """.strip()
                );
    }

    @Test
    void dashboardReviewCardMustRequireReviewView()
            throws IOException {

        String html = normalize(
                read("templates/admin/dashboard.html")
        );

        assertThat(html)
                .contains(
                        """
                        <div class="col-sm-6 col-xl-3" \
                        th:if="${canViewReviews}"> \
                        <a th:href="@{/resenas}"
                        """.strip()
                );
    }

    @Test
    void productLinkMustRequireProductView()
            throws IOException {

        String html = reviews();

        assertThat(html)
                .contains(
                        """
                        <a th:if="${canViewProducts}" \
                        th:href="@{/admin/productos}"
                        """.strip()
                );
    }

    private String reviews() throws IOException {
        return normalize(
                read("templates/admin/reviews.html")
        );
    }

    private String read(
            String resource
    ) throws IOException {

        return new ClassPathResource(resource)
                .getContentAsString(
                        StandardCharsets.UTF_8
                );
    }

    private String normalize(String value) {
        return value
                .replaceAll("\\s+", " ")
                .trim();
    }
}
