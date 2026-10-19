package com.webempresarial.store.view;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class OrderAdminViewContractTest {

    @Test
    void dashboardOrderLinksMustRequireOrderView()
            throws IOException {

        String html = normalize(
                read("templates/admin/dashboard.html")
        );

        assertThat(html)
                .contains(
                        """
                        <div class="col-md-3" \
                        th:if="${canViewOrders}">
                        """.strip()
                )
                .contains(
                        """
                        <div class="col-sm-6 col-xl-3" \
                        th:if="${canViewOrders}"> \
                        <a th:href="@{/orders}"
                        """.strip()
                );
    }

    @Test
    void orderListUpdateActionsMustRequireOrderUpdate()
            throws IOException {

        String html = normalize(
                read("templates/admin/orders.html")
        );

        assertThat(html)
                .contains(
                        """
                        <form th:if="${canUpdateOrders \
                        and order.paymentMethod ==
                        """.strip()
                )
                .contains(
                        """
                        <a th:if="${canUpdateOrders \
                        and order.paymentStatus ==
                        """.strip()
                )
                .contains(
                        """
                        <button th:if="${canUpdateOrders \
                        and order.paymentStatus ==
                        """.strip()
                )
                .contains(
                        "mark-delivered-button"
                )
                .contains(
                        "/status-ajax"
                );
    }

    @Test
    void orderCancellationMustRequireOrderCancel()
            throws IOException {

        String html = normalize(
                read("templates/admin/orders.html")
        );

        assertThat(html)
                .contains(
                        """
                        <form th:if="${canCancelOrders \
                        and order.paymentStatus !=
                        """.strip()
                )
                .contains(
                        "th:action=\"@{/orders/{id}/cancel(id=${order.id})}\""
                );
    }

    @Test
    void orderDetailMutationsMustRequireOrderUpdate()
            throws IOException {

        String html = normalize(
                read("templates/admin/order-details.html")
        );

        assertThat(html)
                .contains(
                        """
                        <form th:if="${canUpdateOrders}" \
                        th:action="@{/orders/{id}/confirm-payment(
                        """.strip()
                )
                .contains(
                        """
                        <form th:if="${canUpdateOrders \
                        and order.paid \
                        and !order.delivered}" \
                        th:action="@{/orders/update-shipping}"
                        """.strip()
                );
    }

    @Test
    void inventoryOrderLinksMustRequireOrderView()
            throws IOException {

        String detail = normalize(
                read(
                        "templates/admin/inventory/"
                        + "product-detail.html"
                )
        );

        String movements = normalize(
                read(
                        "templates/admin/inventory/"
                        + "movements.html"
                )
        );

        assertThat(detail)
                .contains(
                        """
                        <a th:if="${canViewOrders \
                        and movement.orderId != null}" \
                        th:href="@{/orders/{id}(
                        """.strip()
                );

        assertThat(movements)
                .contains(
                        """
                        <a th:if="${canViewOrders \
                        and movement.orderId != null}" \
                        th:href="@{/orders/{id}(id=${movement.orderId})}"
                        """.strip()
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
