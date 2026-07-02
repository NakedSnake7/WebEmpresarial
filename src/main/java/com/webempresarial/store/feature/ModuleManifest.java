package com.webempresarial.store.feature;

import java.util.ArrayList;
import java.util.List;

public final class ModuleManifest {

    private final String name;
    private final String version;
    private final String author;
    private final String vendor;
    private final String website;
    private final String license;
    private final String category;
    private final String minimumPlatformVersion;
    private final List<String> dependencies;

    private ModuleManifest(Builder builder) {
        this.name = builder.name;
        this.version = builder.version;
        this.author = builder.author;
        this.vendor = builder.vendor;
        this.website = builder.website;
        this.license = builder.license;
        this.category = builder.category;
        this.minimumPlatformVersion = builder.minimumPlatformVersion;
        this.dependencies = List.copyOf(builder.dependencies);
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getAuthor() { return author; }
    public String getVendor() { return vendor; }
    public String getWebsite() { return website; }
    public String getLicense() { return license; }
    public String getCategory() { return category; }
    public String getMinimumPlatformVersion() { return minimumPlatformVersion; }
    public List<String> getDependencies() { return dependencies; }

    public static final class Builder {

        private final String name;
        private String version = "1.0.0";
        private String author = "WebEmpresarial";
        private String vendor = "WebEmpresarial";
        private String website = "#";
        private String license = "Proprietary";
        private String category = "Core";
        private String minimumPlatformVersion = "1.0.0";
        private final List<String> dependencies = new ArrayList<>();

        private Builder(String name) {
            this.name = name;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder vendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        public Builder website(String website) {
            this.website = website;
            return this;
        }

        public Builder license(String license) {
            this.license = license;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder minimumPlatformVersion(String minimumPlatformVersion) {
            this.minimumPlatformVersion = minimumPlatformVersion;
            return this;
        }

        public Builder dependsOn(String moduleName) {
            this.dependencies.add(moduleName);
            return this;
        }

        public ModuleManifest build() {
            return new ModuleManifest(this);
        }
    }
}