package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.domain.Compra;
import co.edu.javeriana.proyecto.domain.Orden;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

public class GenerarFacturaPdfUseCase {

    public String ejecutar(Orden orden, String emailUsuario, String rutaDestinoDir) {
        Document document = new Document();
        String nombreArchivo = rutaDestinoDir + File.separator + "Factura_" + orden.getId() + ".pdf";

        try {
            PdfWriter.getInstance(document, new FileOutputStream(nombreArchivo));
            document.open();

            // Titulo
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
            Paragraph titulo = new Paragraph("Factura de Compra - MyLib", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(Chunk.NEWLINE);

            // Informacion General
            Font fontTexto = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            document.add(new Paragraph("Orden ID: " + orden.getId(), fontTexto));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            document.add(new Paragraph("Fecha: " + orden.getFecha().format(formatter), fontTexto));
            document.add(new Paragraph("Cliente: " + emailUsuario, fontTexto));
            document.add(new Paragraph("Estado: " + orden.getEstado(), fontTexto));
            document.add(Chunk.NEWLINE);

            // Tabla de productos
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 1f, 2f, 2f});

            addTableHeader(table, "Producto");
            addTableHeader(table, "Cant");
            addTableHeader(table, "Precio Unit.");
            addTableHeader(table, "Subtotal");

            double subtotalItems = 0;
            for (Compra item : orden.getItems()) {
                table.addCell(item.getLibro().getTitulo());
                table.addCell(String.valueOf(item.getCantidad()));
                table.addCell(String.format("$%.2f", item.getPrecioUnitario()));
                table.addCell(String.format("$%.2f", item.getSubtotal()));
                subtotalItems += item.getSubtotal();
            }
            document.add(table);
            document.add(Chunk.NEWLINE);

            // Totales
            double impuestos = subtotalItems * 0.19;
            double envio = 5.0;
            double total = subtotalItems + impuestos + envio;

            Paragraph pSubtotal = new Paragraph(String.format("Subtotal: $%.2f", subtotalItems), fontTexto);
            pSubtotal.setAlignment(Element.ALIGN_RIGHT);
            document.add(pSubtotal);

            Paragraph pImpuestos = new Paragraph(String.format("IVA (19%%): $%.2f", impuestos), fontTexto);
            pImpuestos.setAlignment(Element.ALIGN_RIGHT);
            document.add(pImpuestos);

            Paragraph pEnvio = new Paragraph(String.format("Envio: $%.2f", envio), fontTexto);
            pEnvio.setAlignment(Element.ALIGN_RIGHT);
            document.add(pEnvio);

            Font fontTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.RED);
            Paragraph pTotal = new Paragraph(String.format("Total Pagado: $%.2f", total), fontTotal);
            pTotal.setAlignment(Element.ALIGN_RIGHT);
            document.add(pTotal);

            document.close();
            return nombreArchivo;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addTableHeader(PdfPTable table, String title) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
        header.setBorderWidth(2);
        header.setPhrase(new Phrase(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        table.addCell(header);
    }
}
