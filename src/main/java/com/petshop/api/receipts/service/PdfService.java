package com.petshop.api.receipts.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.petshop.api.receipts.domain.Receipt;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    private static final Color COLOR_PRIMARY = new Color(29, 158, 117);
    private static final Color COLOR_GRAY    = new Color(120, 120, 120);
    private static final Color COLOR_ROW_ALT = new Color(245, 245, 245);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generateReceipt(Receipt receipt) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(doc, out);
        doc.open();

        // ── Cabeçalho ─────────────────────────────────────────────────────
        Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, COLOR_PRIMARY);
        Font subtitleFont = new Font(Font.HELVETICA, 12, Font.NORMAL, COLOR_GRAY);
        Font labelFont = new Font(Font.HELVETICA, 8, Font.NORMAL, COLOR_GRAY);
        Font valueFont = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font tableHeaderFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        Font tableBodyFont = new Font(Font.HELVETICA, 9);
        Font totalLabelFont = new Font(Font.HELVETICA, 9, Font.NORMAL, COLOR_GRAY);
        Font totalValueFont = new Font(Font.HELVETICA, 9);
        Font grandTotalFont = new Font(Font.HELVETICA, 11, Font.BOLD, COLOR_PRIMARY);

        doc.add(new Paragraph("Petshop", titleFont));
        String typeLabel = receipt.getType().name().equals("PRODUCT") ? "Produto" : "Serviço";
        doc.add(new Paragraph("Recibo de " + typeLabel, subtitleFont));
        doc.add(new Paragraph(" "));

        // ── Dados do recibo ───────────────────────────────────────────────
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(10);

        addInfoCell(infoTable, "Nº do Recibo", receipt.getNumber(), labelFont, valueFont);
        addInfoCell(infoTable, "Data", receipt.getCreatedAt().format(FMT), labelFont, valueFont);
        addInfoCell(infoTable, "Cliente", receipt.getCustomerName(), labelFont, valueFont);
        addInfoCell(infoTable, "Pet",
                receipt.getPetName() != null ? receipt.getPetName() : "—", labelFont, valueFont);
        if (receipt.getSchedulingId() != null) {
            addInfoCell(infoTable, "Agendamento", "#" + receipt.getSchedulingId(), labelFont, valueFont);
            addInfoCell(infoTable, "", "", labelFont, valueFont);
        }
        doc.add(infoTable);

        // ── Itens ─────────────────────────────────────────────────────────
        PdfPTable itemsTable = new PdfPTable(4);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[]{5, 1.5f, 2, 2});
        itemsTable.setSpacingAfter(10);

        for (String h : new String[]{"Descrição", "Qtd", "Preço Unit.", "Total"}) {
            PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
            cell.setBackgroundColor(COLOR_PRIMARY);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            itemsTable.addCell(cell);
        }

        for (int i = 0; i < receipt.getItems().size(); i++) {
            var item = receipt.getItems().get(i);
            Color rowBg = i % 2 == 0 ? Color.WHITE : COLOR_ROW_ALT;

            addItemCell(itemsTable, item.getDescription(), rowBg, tableBodyFont, Element.ALIGN_LEFT);
            addItemCell(itemsTable, item.getQuantity().stripTrailingZeros().toPlainString(), rowBg, tableBodyFont, Element.ALIGN_CENTER);
            addItemCell(itemsTable, formatBrl(item.getUnitPrice()), rowBg, tableBodyFont, Element.ALIGN_RIGHT);
            addItemCell(itemsTable, formatBrl(item.getTotal()), rowBg, tableBodyFont, Element.ALIGN_RIGHT);
        }
        doc.add(itemsTable);

        // ── Totais ────────────────────────────────────────────────────────
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(40);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.setSpacingAfter(10);

        addTotalRow(totalsTable, "Subtotal", formatBrl(receipt.getSubtotal()), totalLabelFont, totalValueFont);

        if (receipt.getDiscount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            addTotalRow(totalsTable, "Desconto", "- " + formatBrl(receipt.getDiscount()), totalLabelFont, totalValueFont);
        }

        addTotalRow(totalsTable, "TOTAL", formatBrl(receipt.getTotal()), grandTotalFont, grandTotalFont);
        doc.add(totalsTable);

        // ── Observações ───────────────────────────────────────────────────
        if (receipt.getObservations() != null && !receipt.getObservations().isBlank()) {
            Font obsLabelFont = new Font(Font.HELVETICA, 9, Font.BOLD, COLOR_GRAY);
            Font obsFont = new Font(Font.HELVETICA, 9);
            doc.add(new Paragraph("Observações:", obsLabelFont));
            doc.add(new Paragraph(receipt.getObservations(), obsFont));
            doc.add(new Paragraph(" "));
        }

        // ── Rodapé ────────────────────────────────────────────────────────
        Font footerFont = new Font(Font.HELVETICA, 9, Font.NORMAL, COLOR_GRAY);
        Paragraph footer = new Paragraph("Obrigado pela preferência!", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
        return out.toByteArray();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void addInfoCell(PdfPTable table, String label, String value,
                             Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.addElement(new Phrase(label, labelFont));
        cell.addElement(new Phrase(value, valueFont));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingBottom(6);
        table.addCell(cell);
    }

    private void addItemCell(PdfPTable table, String value, Color bg,
                             Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String value,
                             Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(3);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPaddingBottom(3);
        table.addCell(valueCell);
    }

    private String formatBrl(java.math.BigDecimal value) {
        return "R$ " + String.format("%.2f", value).replace(".", ",");
    }
}