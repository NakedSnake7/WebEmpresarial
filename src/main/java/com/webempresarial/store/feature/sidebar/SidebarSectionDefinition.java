package com.webempresarial.store.feature.sidebar;

import java.util.ArrayList;
import java.util.List;

public final class SidebarSectionDefinition {

    private final String title;
    private final String icon;
    private final List<SidebarItemDefinition> items;

    private SidebarSectionDefinition(Builder builder) {
        this.title = builder.title;
        this.icon = builder.icon;
        this.items = List.copyOf(builder.items);
    }

    public static Builder builder(String title, String icon) {
        return new Builder(title, icon);
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public List<SidebarItemDefinition> getItems() {
        return items;
    }

    public static final class Builder {

        private final String title;
        private final String icon;
        private final List<SidebarItemDefinition> items = new ArrayList<>();

        private Builder(String title, String icon) {
            this.title = title;
            this.icon = icon;
        }

        public Builder item(
                String title,
                String icon,
                String url,
                com.webempresarial.store.model.Feature feature
        ) {
            this.items.add(
                    new SidebarItemDefinition(title, icon, url, feature)
            );

            return this;
        }

        public SidebarSectionDefinition build() {
            return new SidebarSectionDefinition(this);
        }
    }
}