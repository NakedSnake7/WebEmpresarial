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

    // Identidad técnica
    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String theme;

    @Column(nullable = false, unique = true)
    private String dominio;

    @Column(nullable = false)
    private boolean activa = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StorePlan plan = StorePlan.BASIC;
    
    @Column(length = 100)
    private String stripeConnectedAccountId;

    @Column(nullable = false)
    private boolean stripeConnected = false;

    private LocalDateTime stripeConnectedAt;

    // Branding / SaaS
    @Column(length = 300)
    private String logoUrl;

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
    
    
    @OneToOne(mappedBy = "store", fetch = FetchType.LAZY)
    private Subscription subscription;
    

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

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
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
}