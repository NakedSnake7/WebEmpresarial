package com.webempresarial.store.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
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
                .findByIdAndLeadStoreId(proposalId, storeId)
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
                    11,
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

            Paragraph brand = new Paragraph(
                    safe(store.getNombre()),
                    titleFont
            );
            brand.setSpacingAfter(6);
            document.add(brand);

            Paragraph subtitle = new Paragraph(
                    "Propuesta comercial generada desde " + safe(store.getDominio()),
                    subtitleFont
            );
            subtitle.setSpacingAfter(26);
            document.add(subtitle);

            document.add(new Paragraph("Datos del cliente", headingFont));
            document.add(new Paragraph("Nombre: " + safe(lead.getNombre()), bodyFont));
            document.add(new Paragraph("Empresa: " + safe(lead.getEmpresa()), bodyFont));
            document.add(new Paragraph("WhatsApp: " + safe(lead.getWhatsapp()), bodyFont));
            document.add(new Paragraph("Servicio solicitado: " + safe(lead.getServicio()), bodyFont));

            addSpace(document, bodyFont);

            document.add(new Paragraph("Propuesta", headingFont));
            document.add(new Paragraph(safe(proposal.getTitle()), strongFont));

            if (proposal.getDescription() != null && !proposal.getDescription().isBlank()) {
                Paragraph description = new Paragraph(
                        proposal.getDescription(),
                        bodyFont
                );
                description.setSpacingBefore(8);
                description.setSpacingAfter(12);
                document.add(description);
            }

            document.add(new Paragraph(
                    "Monto: " + formatMoney(proposal.getAmount()),
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
            document.add(new Paragraph("• La propuesta puede ajustarse según alcance final.", bodyFont));
            document.add(new Paragraph("• Los tiempos de entrega dependen de la entrega de información por parte del cliente.", bodyFont));
            document.add(new Paragraph("• El inicio del proyecto requiere anticipo acordado entre ambas partes.", bodyFont));
            document.add(new Paragraph("• Cambios fuera del alcance inicial pueden cotizarse por separado.", bodyFont));

            addSpace(document, bodyFont);

            document.add(new Paragraph("Firma / aceptación", headingFont));
            document.add(new Paragraph("Cliente: ________________________________", bodyFont));
            document.add(new Paragraph("Fecha: _________________________________", bodyFont));

            addSpace(document, bodyFont);

            Paragraph footer = new Paragraph(
                    safe(store.getNombre()) + " · " + safe(store.getDominio()),
                    subtitleFont
            );
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

    private void addSpace(Document document, Font font) throws Exception {
        document.add(new Paragraph(" ", font));
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

    private String formatMoney(java.math.BigDecimal value) {
        if (value == null) {
            return "$0.00 MXN";
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(
                new Locale("es", "MX")
        );

        return formatter.format(value);
    }
}