package com.github.kraudy.InventoryBackend.service;

import com.github.kraudy.InventoryBackend.dto.OrdenDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenDetalleDTO;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Service
public class OrdenPdfService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String PDF_TEMPLATE = """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                @page { margin: 2cm; size: A4; }
                body { 
                    font-family: Arial, sans-serif; 
                    margin: 0; 
                    padding: 0; 
                    color: #222; 
                    line-height: 1.6; 
                }
                h1 { 
                    text-align: center; 
                    color: #1e3a8a; 
                    margin-bottom: 15px; 
                    font-size: 32px; 
                    font-weight: bold; 
                }
                
                .header { 
                    margin-bottom: 35px; 
                    font-size: 16px; 
                }
                .header p { 
                    margin: 6px 0; 
                }
                
                table { 
                    width: 100%; 
                    border-collapse: collapse; 
                    margin-top: 20px; 
                }
                th, td { 
                    border: 1px solid #444; 
                    padding: 10px; 
                    text-align: left; 
                }
                th { 
                    background-color: #f1f5f9; 
                    font-weight: bold; 
                }
                
                .total { 
                    text-align: right; 
                    font-size: 1.6em; 
                    font-weight: bold; 
                    margin: 35px 0 25px 0; 
                    color: #1e3a8a; 
                }
                
                .barcode-container { 
                    text-align: center; 
                    margin: 10px 0 35px 0; 
                }
                .barcode-container img { 
                    max-width: 380px; 
                    width: 100%; 
                }
                
                .footer { 
                    margin-top: 50px; 
                    text-align: center; 
                    font-size: 0.95em; 
                    color: #666; 
                }
            </style>
        </head>
        <body>
            <h1>ORDEN DE TRABAJO #${id}</h1>
            
            <!-- BARCODE - Directly below title -->
            <div class="barcode-container">
                <img src="data:image/png;base64,${barcode}" alt="Barcode"/>
            </div>

            <div class="header">
                <p><strong>Cliente:</strong> ${clienteNombre}</p>
                <p><strong>Creada por:</strong> ${creadaPor}</p>
                <p><strong>Fecha de creación:</strong> ${fechaCreacion}</p>
                <p><strong>Fecha de vencimiento:</strong> ${fechaVencimiento}</p>
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
                TOTAL: C$${totalMonto}
            </div>

            <div class="footer">
                Generado el ${fechaGeneracion} • Gracias por su preferencia
            </div>
        </body>
        </html>
        """;

    public byte[] generateOrdenPdf(OrdenDTO orden, List<OrdenDetalleDTO> detalles) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            String html = buildHtmlFromTemplate(orden, detalles);

            PdfRendererBuilder builder = new PdfRendererBuilder()
                    .withHtmlContent(html, null)
                    .toStream(baos)
                    .usePdfAConformance(PdfRendererBuilder.PdfAConformance.NONE);

            builder.run();

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF", e);
        }
    }

    private String buildHtmlFromTemplate(OrdenDTO orden, List<OrdenDetalleDTO> detalles) throws Exception {
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

        String barcodeBase64 = generateBarcodeBase64(String.valueOf(orden.id()));

        return PDF_TEMPLATE
                .replace("${id}", String.valueOf(orden.id()))
                .replace("${clienteNombre}", orden.clienteNombre() != null ? orden.clienteNombre() : "—")
                .replace("${creadaPor}", orden.creadaPor() != null ? orden.creadaPor() : "—")
                .replace("${fechaCreacion}", orden.fechaCreacion().format(DATE_FORMAT))
                .replace("${fechaVencimiento}", orden.fechaVencimiento().format(DATE_FORMAT))
                .replace("${detalles}", rows.toString())
                .replace("${totalMonto}", orden.totalMonto() != null ? orden.totalMonto().toString() : "0.00")
                .replace("${fechaGeneracion}", java.time.LocalDateTime.now().format(DATE_FORMAT))
                .replace("${barcode}", barcodeBase64);
    }

    private String generateBarcodeBase64(String text) throws Exception {
        BitMatrix bitMatrix = new Code128Writer().encode(text, BarcodeFormat.CODE_128, 380, 90);
        java.awt.image.BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}