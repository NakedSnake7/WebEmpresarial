package com.webempresarial.store.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.entity.Proposal;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.ProposalRepository;

@Service
public class ProposalPdfService {

    private final ProposalRepository proposalRepository;

    public ProposalPdfService(ProposalRepository proposalRepository) {
        this.proposalRepository = proposalRepository;
    }

    public byte[] generateProposalPdf(Long proposalId, Long storeId) {

        Proposal proposal = proposalRepository
                .findFullProposal(proposalId, storeId)
                .orElseThrow(() -> new RuntimeException(
                        "Propuesta no encontrada para esta tienda"
                ));

        Lead lead = proposal.getLead();
        Store store = lead.getStore();

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            Document document = new Document(
                    PageSize.A4,
                    44,
                    44,
                    48,
                    48
            );

            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    22,
                    new Color(15, 23, 42)
            );

            Font subtitleFont = FontFactory.getFont(
                    FontFactory.HELVETICA,
                    10,
                    new Color(100, 116, 139)
            );

            Font headingFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    14,
                    new Color(15, 23, 42)
            );

            Font bodyFont = FontFactory.getFont(
                    FontFactory.HELVETICA,
                    11,
                    new Color(51, 65, 85)
            );

            Font strongFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    12,
                    new Color(15, 23, 42)
            );

            addLogo(document, store);

            Paragraph brand = new Paragraph(
                    safe(store.getNombre()),
                    titleFont
            );
            brand.setSpacingAfter(4);
            document.add(brand);

            addStoreCommercialInfo(document, store, subtitleFont);

            Paragraph subtitle = new Paragraph(
                    "Propuesta comercial",
                    headingFont
            );
            subtitle.setSpacingBefore(18);
            subtitle.setSpacingAfter(18);
            document.add(subtitle);

            document.add(new Paragraph("Datos del cliente", headingFont));
            document.add(new Paragraph("Nombre: " + safe(lead.getNombre()), bodyFont));
            document.add(new Paragraph("Empresa: " + safe(lead.getEmpresa()), bodyFont));
            document.add(new Paragraph("WhatsApp: " + safe(lead.getWhatsapp()), bodyFont));
            document.add(new Paragraph("Servicio solicitado: " + safe(lead.getServicio()), bodyFont));

            addSpace(document, bodyFont);

            document.add(new Paragraph("Detalle de la propuesta", headingFont));
            document.add(new Paragraph(safe(proposal.getTitle()), strongFont));

            if (hasText(proposal.getDescription())) {
                Paragraph description = new Paragraph(
                        proposal.getDescription(),
                        bodyFont
                );
                description.setSpacingBefore(8);
                description.setSpacingAfter(12);
                document.add(description);
            }

            document.add(new Paragraph(
                    "Monto: " + formatMoney(proposal.getAmount(), store),
                    strongFont
            ));

            document.add(new Paragraph(
                    "Probabilidad de cierre: " + safeNumber(proposal.getCloseProbability()) + "%",
                    bodyFont
            ));

            document.add(new Paragraph(
                    "Estado: " + proposal.getStatus().name(),
                    bodyFont
            ));

            if (proposal.getCreatedAt() != null) {
                document.add(new Paragraph(
                        "Fecha: " + proposal.getCreatedAt()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        bodyFont
                ));
            }

            addSpace(document, bodyFont);

            document.add(new Paragraph("Condiciones generales", headingFont));

            if (hasText(store.getProposalFooter())) {
                Paragraph footerConditions = new Paragraph(
                        store.getProposalFooter(),
                        bodyFont
                );
                footerConditions.setSpacingBefore(6);
                document.add(footerConditions);
            } else {
                document.add(new Paragraph(
                        "La propuesta puede ajustarse según el alcance final. " +
                                "Los tiempos de entrega dependen de la entrega de información por parte del cliente. " +
                                "Cambios fuera del alcance inicial pueden cotizarse por separado.",
                        bodyFont
                ));
            }

            addSpace(document, bodyFont);

            document.add(new Paragraph("Firma / aceptación", headingFont));
            document.add(new Paragraph("Cliente: ________________________________", bodyFont));
            document.add(new Paragraph("Fecha: _________________________________", bodyFont));

            addSpace(document, bodyFont);

            Paragraph footer = new Paragraph(
                    buildFooter(store),
                    subtitleFont
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(18);
            document.add(footer);

            document.close();

            return output.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "No se pudo generar el PDF de la propuesta",
                    e
            );
        }
    }

    private void addLogo(Document document, Store store) {
        if (!hasText(store.getLogoUrl())) {
            return;
        }

        try {
            Image logo = Image.getInstance(new URL(store.getLogoUrl()));
            logo.scaleToFit(110, 70);
            logo.setAlignment(Element.ALIGN_LEFT);
            logo.setSpacingAfter(10);

            document.add(logo);
        } catch (Exception ignored) {
            // Si el logo falla, el PDF se sigue generando.
        }
    }

    private void addStoreCommercialInfo(
            Document document,
            Store store,
            Font font
    ) throws Exception {

        if (hasText(store.getCompanyEmail())) {
            document.add(new Paragraph(store.getCompanyEmail(), font));
        }

        if (hasText(store.getCompanyPhone())) {
            document.add(new Paragraph(store.getCompanyPhone(), font));
        }

        if (hasText(store.getCompanyWebsite())) {
            document.add(new Paragraph(store.getCompanyWebsite(), font));
        }

        if (hasText(store.getCompanyAddress())) {
            document.add(new Paragraph(store.getCompanyAddress(), font));
        }
    }

    private String buildFooter(Store store) {
        StringBuilder builder = new StringBuilder();

        builder.append(safe(store.getNombre()));

        if (hasText(store.getDominio())) {
            builder.append(" · ").append(store.getDominio());
        }

        if (hasText(store.getCompanyEmail())) {
            builder.append(" · ").append(store.getCompanyEmail());
        }

        if (hasText(store.getCompanyPhone())) {
            builder.append(" · ").append(store.getCompanyPhone());
        }

        return builder.toString();
    }

    private void addSpace(Document document, Font font) throws Exception {
        document.add(new Paragraph(" ", font));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String safe(String value) {
        return value == null || value.isBlank()
                ? "No especificado"
                : value;
    }

    private String safeNumber(Integer value) {
        return value == null
                ? "0"
                : String.valueOf(value);
    }

    private String formatMoney(
            java.math.BigDecimal value,
            Store store
    ) {
        if (value == null) {
            value = java.math.BigDecimal.ZERO;
        }

        String currency = store.getCurrency();

        if (currency == null || currency.isBlank()) {
            currency = "MXN";
        }

        Locale locale = switch (currency.toUpperCase()) {
            case "USD" -> Locale.US;
            case "EUR" -> Locale.GERMANY;
            default -> new Locale("es", "MX");
        };

        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);

        return formatter.format(value) + " " + currency.toUpperCase();
    }
}