package com.webempresarial.store.feature.sidebar;

import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.Feature;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SidebarItemDefinitionTest {

    @Test
    void defaultOperationalItemShouldIncludeOwnerButNotSales() {

        SidebarItemDefinition item =
                new SidebarItemDefinition(
                        "Productos",
                        "📦",
                        "/admin/productos",
                        Feature.PRODUCTS
                );

        assertThat(
                item.allows(AdminRole.SUPER_ADMIN)
        ).isTrue();

        assertThat(
                item.allows(AdminRole.STORE_OWNER)
        ).isTrue();

        assertThat(
                item.allows(AdminRole.STORE_ADMIN)
        ).isTrue();

        assertThat(
                item.allows(AdminRole.STORE_STAFF)
        ).isTrue();

        assertThat(
                item.allows(AdminRole.SALES)
        ).isFalse();
    }
}
