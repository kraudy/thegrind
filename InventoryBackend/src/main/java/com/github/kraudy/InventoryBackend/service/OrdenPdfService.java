package com.github.kraudy.InventoryBackend.service;

import com.github.kraudy.InventoryBackend.dto.OrdenDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenDetalleDTO;
import com.lowagie.text.Document;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrdenPdfService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String PDF_TEMPLATE = """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; margin: 30px; color: #222; line-height: 1.5; }
                h1 { text-align: center; color: #1e3a8a; margin-bottom: 10px; }
                .header { margin-bottom: 30px; }
                table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                th, td { border: 1px solid #ccc; padding: 10px; text-align: left; }
                th { background-color: #f8fafc; font-weight: bold; }
                .total { text-align: right; font-size: 1.3em; font-weight: bold; margin-top: 20px; color: #1e3a8a; }
                .footer { margin-top: 50px; text-align: center; font-size: 0.9em; color: #666; }
            </style>
        </head>
        <body>
            <h1>Orden de Trabajo #${id}</h1>
            
            <div class="header">
                <p><strong>Cliente:</strong> ${clienteNombre}</p>
                <p><strong>Estado:</strong> ${estado}</p>
                <p><strong>Fecha de creación:</strong> ${fechaCreacion}</p>
                <p><strong>Fecha de vencimiento:</strong> ${fechaVencimiento}</p>
                <p><strong>Creada por:</strong> ${creadaPor}</p>
            </div>

            <table>
                <thead>
                    <tr>
                        <th>Línea</th>
                        <th>Producto</th>
                        <th>Cantidad</th>
                        <th>Precio Unitario</th>
                        <th>Subtotal</th>
                    </tr>
                </thead>
                <tbody>
                    ${detalles}
                </tbody>
            </table>

            <div class="total">
                TOTAL: $${totalMonto}
            </div>

            <div class="footer">
                Generado el ${fechaGeneracion} • Gracias por su preferencia
            </div>
        </body>
        </html>
        """;

    public byte[] generateOrdenPdf(OrdenDTO orden, List<OrdenDetalleDTO> detalles) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            String html = buildHtmlFromTemplate(orden, detalles);

            HTMLWorker htmlWorker = new HTMLWorker(document);
            htmlWorker.parse(new StringReader(html));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF", e);
        }
    }

    private String buildHtmlFromTemplate(OrdenDTO orden, List<OrdenDetalleDTO> detalles) {
        StringBuilder rows = new StringBuilder();

        int linea = 1;
        for (OrdenDetalleDTO det : detalles) {
            rows.append(String.format("""
                <tr>
                    <td>%d</td>
                    <td>%s</td>
                    <td>%d</td>
                    <td>%.2f</td>
                    <td>%.2f</td>
                </tr>
                """, linea++, det.nombreProducto(), det.cantidad(), det.precioUnitario(), det.subtotal()));
        }

        return PDF_TEMPLATE
                .replace("${id}", String.valueOf(orden.id()))
                .replace("${clienteNombre}", orden.clienteNombre() != null ? orden.clienteNombre() : "—")
                .replace("${estado}", orden.estado() != null ? orden.estado() : "—")
                .replace("${fechaCreacion}", orden.fechaCreacion().format(DATE_FORMAT))
                .replace("${fechaVencimiento}", orden.fechaVencimiento().format(DATE_FORMAT))
                .replace("${creadaPor}", orden.creadaPor() != null ? orden.creadaPor() : "—")
                .replace("${detalles}", rows.toString())
                .replace("${totalMonto}", orden.totalMonto() != null ? orden.totalMonto().toString() : "0.00")
                .replace("${fechaGeneracion}", java.time.LocalDateTime.now().format(DATE_FORMAT));
    }
}