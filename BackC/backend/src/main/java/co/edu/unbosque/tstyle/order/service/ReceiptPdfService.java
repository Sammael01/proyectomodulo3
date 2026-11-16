package co.edu.unbosque.tstyle.order.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

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

import co.edu.unbosque.tstyle.common.MoneyFormatter;
import co.edu.unbosque.tstyle.order.dto.OrderDetailResponse;
import co.edu.unbosque.tstyle.order.dto.OrderItemResponse;

/**
 * Genera el comprobante de compra en PDF.
 *
 * <p>El PDF se arma al vuelo con los datos guardados del pedido, por lo que
 * siempre coincide con lo que se cobró y no hay que almacenar archivos.</p>
 *
 * @author Equipo T Style
 */
@Service
public class ReceiptPdfService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Genera el comprobante de un pedido.
     *
     * @param order detalle del pedido
     * @return bytes del archivo PDF
     * @throws IllegalStateException si no fue posible generar el PDF
     */
    public byte[] generate(OrderDetailResponse order) {

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

            Paragraph title = new Paragraph("T Style", titleFont);
            document.add(title);

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

            addHeaderCell(table, "Producto", Element.ALIGN_LEFT, boldFont);
            addHeaderCell(table, "Cantidad", Element.ALIGN_CENTER, boldFont);
            addHeaderCell(table, "Precio unitario", Element.ALIGN_RIGHT, boldFont);
            addHeaderCell(table, "Subtotal", Element.ALIGN_RIGHT, boldFont);

            for (OrderItemResponse item : order.items()) {
                addBodyCell(table, item.productName(), Element.ALIGN_LEFT, normalFont);
                addBodyCell(table, String.valueOf(item.quantity()), Element.ALIGN_CENTER, normalFont);
                addBodyCell(table, MoneyFormatter.format(item.unitPrice()), Element.ALIGN_RIGHT, normalFont);
                addBodyCell(table, MoneyFormatter.format(item.subtotal()), Element.ALIGN_RIGHT, normalFont);
            }

            document.add(table);

            Paragraph total = new Paragraph("Total: " + MoneyFormatter.format(order.total()), totalFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            total.setSpacingBefore(14);
            document.add(total);

            Paragraph note = new Paragraph(
                    "Compra realizada con pago simulado. Este documento no tiene validez fiscal.",
                    noteFont);
            note.setSpacingBefore(30);
            document.add(note);

            document.close();

            return output.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException("No fue posible generar el comprobante PDF", e);
        }
    }

    private void addHeaderCell(PdfPTable table, String text, int alignment, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setBackgroundColor(new Color(230, 230, 230));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, int alignment, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(6);
        table.addCell(cell);
    }
}
