package com.github.kraudy.InventoryBackend.service.pdf;

import com.github.kraudy.InventoryBackend.dto.OrdenCostoDTO;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CostoPdfService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String PDF_TEMPLATE = """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                @page { 
                    margin: 2cm; 
                    size: legal landscape;   /* ← Legal + Horizontal (landscape) */
                }
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
                    margin-bottom: 8px; 
                    font-size: 32px; 
                    font-weight: bold; 
                }
                .subtitle {
                    text-align: center;
                    color: #334155;
                    font-size: 20px;
                    margin-bottom: 30px;
                }
                .header { 
                    margin-bottom: 35px; 
                    font-size: 16px; 
                    background-color: #f8fafc;
                    padding: 15px;
                    border-radius: 8px;
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
                    padding: 10px 12px; 
                    text-align: left; 
                    font-size: 14px;
                }
                th { 
                    background-color: #f1f5f9; 
                    font-weight: bold; 
                    color: #1e3a8a;
                }
                .total { 
                    text-align: right; 
                    font-size: 1.8em; 
                    font-weight: bold; 
                    color: #1e3a8a; 
                    padding: 15px;
                    border: 3px solid #1e3a8a;
                    border-radius: 12px;
                    display: block;
                    width: fit-content;
                }
                .signature {
                    text-align: center;
                    page-break-inside: avoid;
                }
                .signature-line {
                    border-top: 2px solid #333;
                    width: 380px;
                    margin: 0 auto 8px;
                }
                .footer { 
                    margin-top: 60px;
                    text-align: center; 
                    font-size: 0.9em; 
                    color: #666; 
                }
                /* New flex container for signature + total */
                .bottom-section {
                    display: flex;
                    justify-content: space-between;
                    align-items: flex-end;
                    margin-top: 50px;
                    gap: 40px;
                }
            </style>
        </head>
        <body>
            <h1>RECIBO DE PAGO</h1>
            <div class="subtitle">Tipo: <strong>${tipoCosto}</strong> • Trabajador: <strong>${trabajador}</strong></div>

            <div class="header">
                <p><strong>Trabajador:</strong> ${trabajador}</p>
                <p><strong>Tipo de Costo:</strong> ${tipoCosto}</p>
                <p><strong>Fecha de generación:</strong> ${fechaGeneracion}</p>
                <p><strong>Cantidad de registros:</strong> ${cantidadRegistros}</p>
            </div>

            <table>
                <thead>
                    <tr>
                        <th>ID Orden</th>
                        <th>ID Detalle</th>
                        <th>Cliente</th>
                        <th>Cant. Asignada</th>
                        <th>Cant. Trabajada</th>
                        <th>Costo Unit.</th>
                        <th>Subtotal</th>
                        <th>Fecha Trabajo</th>
                        <th>Comentario</th>
                    </tr>
                </thead>
                <tbody>
                    ${detalles}
                </tbody>
            </table>

            <!-- Bottom section: signature left + total right -->
            <div class="bottom-section">
                <!-- Firma -->
                <div class="signature">
                    <div class="signature-line"></div>
                    <p style="margin: 0;"><strong>Firma del Trabajador</strong></p>
                    <p style="margin: 4px 0 0 0;">${trabajador}</p>
                    <p style="margin-top: 30px; font-size: 14px;">_______________________________</p>
                    <p style="margin: 4px 0 0 0;">Fecha y Hora de Firma</p>
                </div>

                <div class="total">
                    TOTAL A PAGAR: C$${totalMonto}
                </div>
            </div>

            <div class="footer">
                Generado el ${fechaGeneracion} • Sistema de Gestión de Órdenes
            </div>
        </body>
        </html>
        """;

    public byte[] generateCostoPdf(List<OrdenCostoDTO> costos, String tipoCosto, String trabajador, BigDecimal total) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            String html = buildHtmlFromTemplate(costos, tipoCosto, trabajador, total);

            PdfRendererBuilder builder = new PdfRendererBuilder()
                    .withHtmlContent(html, null)
                    .toStream(baos)
                    .usePdfAConformance(PdfRendererBuilder.PdfAConformance.NONE);

            builder.run();

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF de recibo de costo", e);
        }
    }

    private String buildHtmlFromTemplate(List<OrdenCostoDTO> costos, String tipoCosto,
                                         String trabajador, BigDecimal total) {

        StringBuilder rows = new StringBuilder();

        for (OrdenCostoDTO c : costos) {
            String fechaTrabajo = c.fechaTrabajo() != null 
                    ? c.fechaTrabajo().format(DATE_ONLY_FORMAT) 
                    : "—";

            rows.append(String.format("""
                <tr>
                    <td>%d</td>
                    <td>%d</td>
                    <td>%s</td>
                    <td style="text-align:center;">%d</td>
                    <td style="text-align:center;">%d</td>
                    <td style="text-align:right;">%.2f</td>
                    <td style="text-align:right; font-weight:bold;">%.2f</td>
                    <td>%s</td>
                    <td style="max-width:220px;">%s</td>
                </tr>
                """,
                c.idOrden(),
                c.idOrdenDetalle(),
                c.clienteNombre() != null ? c.clienteNombre() : "—",
                c.cantidadAsignada(),
                c.cantidadTrabajada(),
                c.costo() != null ? c.costo() : BigDecimal.ZERO,
                c.subTotal() != null ? c.subTotal() : BigDecimal.ZERO,
                fechaTrabajo,
                c.comentario() != null ? c.comentario().replace("\n", "<br>") : "—"
            ));
        }

        return PDF_TEMPLATE
                .replace("${tipoCosto}", tipoCosto)
                .replace("${trabajador}", trabajador)
                .replace("${fechaGeneracion}", LocalDateTime.now().format(DATE_FORMAT))
                .replace("${cantidadRegistros}", String.valueOf(costos.size()))
                .replace("${detalles}", rows.toString())
                .replace("${totalMonto}", total != null ? total.setScale(2, RoundingMode.HALF_UP).toPlainString() : "0.00");
    }
}