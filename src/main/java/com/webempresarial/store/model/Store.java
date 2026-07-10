package com.webempresarial.store.model;

import java.time.LocalDateTime;

import com.webempresarial.store.entity.Subscription;

import jakarta.persistence.*;

@Entity
@Table(
    name = "stores",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_store_dominio",
            columnNames = "dominio"
        )
    }
)
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================================
    // IDENTIDAD
    // =====================================================

    @Column(nullable = false)
    private String nombre;

    /**
     * Theme físico actual.
     *
     * Ejemplos:
     * stride
     * barleypunch
     * WebEmpresarial
     * commerce-modern
     * commerce-luxury
     */
    @Enumerated(EnumType.STRING)
    private ThemeType themeType;

	private String theme;

    @Column(nullable = false)
    private String dominio;

    @Column(nullable = false)
    private boolean activa = true;

    /**
     * Compatibility cache.
     *
     * The source of truth for commercial permissions is Subscription.plan
     * together with Subscription.status.
     *
     * Do not use this field to authorize features.
     * Use PlatformAccessService / FeatureAccessService instead.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StorePlan plan = StorePlan.BASIC;

    // =====================================================
    // STRIPE CONNECT
    // =====================================================

    @Column(length = 100)
    private String stripeConnectedAccountId;

    @Column(nullable = false)
    private boolean stripeConnected = false;

    private LocalDateTime stripeConnectedAt;

    // =====================================================
    // BRANDING WHITE LABEL
    // =====================================================

    @Column(length = 300)
    private String logoUrl;

    @Column(length = 300)
    private String faviconUrl;

    @Column(length = 20)
    private String primaryColor = "#111827";

    @Column(length = 20)
    private String secondaryColor = "#6B7280";

    @Column(length = 20)
    private String accentColor = "#2563EB";

    @Column(length = 80)
    private String fontFamily = "Inter";

    @Column(length = 300)
    private String heroImageUrl;

    @Column(length = 120)
    private String slogan;

    // =====================================================
    // DATOS COMERCIALES
    // =====================================================

    @Column(length = 150)
    private String companyEmail;

    @Column(length = 50)
    private String companyPhone;

    @Column(length = 250)
    private String companyAddress;

    @Column(length = 200)
    private String companyWebsite;

    @Column(length = 120)
    private String contactName;

    @Column(length = 3)
    private String currency = "MXN";

    @Column(columnDefinition = "TEXT")
    private String proposalFooter;

    // =====================================================
    // SUSCRIPCIÓN
    // =====================================================

    @OneToOne(mappedBy = "store", fetch = FetchType.LAZY)
    private Subscription subscription;

    // =====================================================
    // GETTERS & SETTERS
    // =====================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getDominio() {
        return dominio;
    }

    public void setDominio(String dominio) {
        this.dominio = dominio;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public StorePlan getPlan() {
        return plan;
    }

    public void setPlan(StorePlan plan) {
        this.plan = plan;
    }

    public String getStripeConnectedAccountId() {
        return stripeConnectedAccountId;
    }

    public void setStripeConnectedAccountId(String stripeConnectedAccountId) {
        this.stripeConnectedAccountId = stripeConnectedAccountId;
    }

    public boolean isStripeConnected() {
        return stripeConnected;
    }

    public void setStripeConnected(boolean stripeConnected) {
        this.stripeConnected = stripeConnected;
    }

    public LocalDateTime getStripeConnectedAt() {
        return stripeConnectedAt;
    }

    public void setStripeConnectedAt(LocalDateTime stripeConnectedAt) {
        this.stripeConnectedAt = stripeConnectedAt;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getFaviconUrl() {
        return faviconUrl;
    }

    public void setFaviconUrl(String faviconUrl) {
        this.faviconUrl = faviconUrl;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public String getHeroImageUrl() {
        return heroImageUrl;
    }

    public void setHeroImageUrl(String heroImageUrl) {
        this.heroImageUrl = heroImageUrl;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getCompanyWebsite() {
        return companyWebsite;
    }

    public void setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getProposalFooter() {
        return proposalFooter;
    }

    public void setProposalFooter(String proposalFooter) {
        this.proposalFooter = proposalFooter;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }
    public ThemeType getThemeType() {
		return themeType;
	}

	public void setThemeType(ThemeType themeType) {
		this.themeType = themeType;
	}

}