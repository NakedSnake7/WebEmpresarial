package com.webempresarial.store.entity;

import com.webempresarial.store.model.Store;
import jakarta.persistence.*;

@Entity
@Table(name = "store_settings")
public class StoreSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false, unique = true)
    private Store store;

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
    
    @Column(length = 160)
    private String heroTitle;

    @Column(columnDefinition = "TEXT")
    private String heroSubtitle;

    @Column(length = 80)
    private String heroEyebrow;

    @Column(length = 80)
    private String heroButtonText;

    @Column(length = 200)
    private String heroButtonUrl;

    @Column(length = 160)
    private String aboutTitle;

    @Column(columnDefinition = "TEXT")
    private String aboutText;

    @Column(length = 160)
    private String ctaTitle;

    @Column(columnDefinition = "TEXT")
    private String ctaText;

    @Column(length = 300)
    private String whatsappMessage;

    @Column(length = 300)
    private String facebookUrl;

    @Column(length = 300)
    private String instagramUrl;

    @Column(length = 300)
    private String tiktokUrl;

    @Column(columnDefinition = "TEXT")
    private String footerText;

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
    


    public Long getId() {
        return id;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
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

	public String getHeroTitle() {
		return heroTitle;
	}

	public void setHeroTitle(String heroTitle) {
		this.heroTitle = heroTitle;
	}

	public String getHeroSubtitle() {
		return heroSubtitle;
	}

	public void setHeroSubtitle(String heroSubtitle) {
		this.heroSubtitle = heroSubtitle;
	}

	public String getHeroEyebrow() {
		return heroEyebrow;
	}

	public void setHeroEyebrow(String heroEyebrow) {
		this.heroEyebrow = heroEyebrow;
	}

	public String getHeroButtonText() {
		return heroButtonText;
	}

	public void setHeroButtonText(String heroButtonText) {
		this.heroButtonText = heroButtonText;
	}

	public String getHeroButtonUrl() {
		return heroButtonUrl;
	}

	public void setHeroButtonUrl(String heroButtonUrl) {
		this.heroButtonUrl = heroButtonUrl;
	}

	public String getAboutTitle() {
		return aboutTitle;
	}

	public void setAboutTitle(String aboutTitle) {
		this.aboutTitle = aboutTitle;
	}

	public String getAboutText() {
		return aboutText;
	}

	public void setAboutText(String aboutText) {
		this.aboutText = aboutText;
	}

	public String getCtaTitle() {
		return ctaTitle;
	}

	public void setCtaTitle(String ctaTitle) {
		this.ctaTitle = ctaTitle;
	}

	public String getCtaText() {
		return ctaText;
	}

	public void setCtaText(String ctaText) {
		this.ctaText = ctaText;
	}

	public String getWhatsappMessage() {
		return whatsappMessage;
	}

	public void setWhatsappMessage(String whatsappMessage) {
		this.whatsappMessage = whatsappMessage;
	}

	public String getFacebookUrl() {
		return facebookUrl;
	}

	public void setFacebookUrl(String facebookUrl) {
		this.facebookUrl = facebookUrl;
	}

	public String getInstagramUrl() {
		return instagramUrl;
	}

	public void setInstagramUrl(String instagramUrl) {
		this.instagramUrl = instagramUrl;
	}

	public String getTiktokUrl() {
		return tiktokUrl;
	}

	public void setTiktokUrl(String tiktokUrl) {
		this.tiktokUrl = tiktokUrl;
	}

	public String getFooterText() {
		return footerText;
	}

	public void setFooterText(String footerText) {
		this.footerText = footerText;
	}

	public void setId(Long id) {
		this.id = id;
	}
    
    
}