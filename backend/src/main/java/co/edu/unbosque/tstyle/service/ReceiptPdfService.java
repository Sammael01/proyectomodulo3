package co.edu.unbosque.tstyle.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import co.edu.unbosque.tstyle.dto.OrderItemResponse;
import co.edu.unbosque.tstyle.dto.OrderResponse;

@Service
public class ReceiptPdfService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String money(BigDecimal value) {
        BigDecimal safeValue = (value == null) ? BigDecimal.ZERO : value;

        DecimalFormat format = new DecimalFormat("#,##0.00",
                DecimalFormatSymbols.getInstance(Locale.of("es", "CO")));

        return "$ " + format.format(safeValue);
    }

    public byte[] generate(OrderResponse order) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
            Font noteFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

            document.add(new Paragraph("T Style", titleFont));

            Paragraph subtitle = new Paragraph("Comprobante de compra", subtitleFont);
            subtitle.setSpacingAfter(18);
            document.add(subtitle);

            document.add(new Paragraph("Número de pedido: " + order.orderNumber(), boldFont));
            document.add(new Paragraph("Fecha: " + order.createdAt().format(DATE_FORMAT), normalFont));
            document.add(new Paragraph("Comprador: " + order.buyerName(), normalFont));
            document.add(new Paragraph("Correo: " + order.buyerEmail(), normalFont));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 4.5f, 1.2f, 2.2f, 2.2f });
            table.setSpacingBefore(18);
            table.setHeaderRows(1);

            addCell(table, "Producto", Element.ALIGN_LEFT, boldFont, true);
            addCell(table, "Cantidad", Element.ALIGN_CENTER, boldFont, true);
            addCell(table, "Precio unitario", Element.ALIGN_RIGHT, boldFont, true);
            addCell(table, "Subtotal", Element.ALIGN_RIGHT, boldFont, true);

            for (OrderItemResponse item : order.items()) {
                addCell(table, item.productName(), Element.ALIGN_LEFT, normalFont, false);
                addCell(table, String.valueOf(item.quantity()), Element.ALIGN_CENTER, normalFont, false);
                addCell(table, money(item.unitPrice()), Element.ALIGN_RIGHT, normalFont, false);
                addCell(table, money(item.subtotal()), Element.ALIGN_RIGHT, normalFont, false);
            }

            document.add(table);

            Paragraph total = new Paragraph("Total: " + money(order.total()), totalFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            total.setSpacingBefore(14);
            document.add(total);

            Paragraph note = new Paragraph(
                    "Compra realizada con pago simulado. Este documento no tiene validez fiscal.", noteFont);
            note.setSpacingBefore(30);
            document.add(note);

            document.close();

            return output.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible generar el comprobante PDF", e);
        }
    }

    private void addCell(PdfPTable table, String text, int alignment, Font font, boolean header) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(6);

        if (header) {
            cell.setBackgroundColor(new Color(230, 230, 230));
        }

        table.addCell(cell);
    }
}
