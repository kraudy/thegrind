package com.github.kraudy.InventoryBackend.service.pdf;

import com.github.kraudy.InventoryBackend.dto.OrdenCostoDTO;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;

@Service
public class CostoPdfService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DIA_NOMBRE_FORMAT = DateTimeFormatter.ofPattern("EEEE", new Locale("es", "NI")); // Lunes, Martes...

    private static final String PDF_TEMPLATE = """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                @page { 
                    margin: 2cm; 
                    size: letter;   /* ← Portrait + Carta (Letter) */
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
                <p><strong>Cantidad de trabajada:</strong> ${cantidadTrabajada}</p>
            </div>

            <table>
                <thead>
                    <tr>
                        <th>Fecha Trabajo</th>
                        <th>Día</th>
                        <th>Cantidad Trabajada</th>
                        <th>Sub Total (C$)</th>
                    </tr>
                </thead>
                <tbody>
                    ${resumenDias}
                </tbody>
            </table>

            <!-- Bottom section: firma izquierda + total derecha -->
            <div class="bottom-section">
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

        // ==================== AGRUPACIÓN POR DÍA ====================
        Map<LocalDate, BigDecimal> dailyTotals = new TreeMap<>();
        Map<LocalDate, Integer> dailyCounts = new TreeMap<>();

        for (OrdenCostoDTO costo : costos) {
            LocalDate fecha = costo.fechaTrabajo();
            if (fecha != null) {
                BigDecimal subTotal = costo.subTotal() != null ? costo.subTotal() : BigDecimal.ZERO;
                Integer cantidad = 0 ;
                if ("Reparacion".equalsIgnoreCase(costo.tipoCosto())) {
                  cantidad = costo.cantidadTrabajada();
                } else if ("Pegado".equalsIgnoreCase(costo.tipoCosto())) {
                  cantidad = costo.cantidadAsignada();
                } 
                dailyTotals.merge(fecha, subTotal, BigDecimal::add);
                dailyCounts.merge(fecha, cantidad, Integer::sum);
            }
        }

        // Construir filas del resumen
        StringBuilder rows = new StringBuilder();
        for (Map.Entry<LocalDate, BigDecimal> entry : dailyTotals.entrySet()) {
            LocalDate date = entry.getKey();
            String diaNombre = date.format(DIA_NOMBRE_FORMAT);
            int count = dailyCounts.get(date);
            BigDecimal dailyTotal = entry.getValue();

            rows.append(String.format("""
                <tr>
                    <td style="text-align:center;">%s</td>
                    <td>%s</td>
                    <td style="text-align:center;">%d</td>
                    <td style="text-align:right; font-weight:bold;">%.2f</td>
                </tr>
                """,
                date.format(DATE_ONLY_FORMAT),
                diaNombre,
                count,
                dailyTotal
            ));
        }

        return PDF_TEMPLATE
                .replace("${tipoCosto}", tipoCosto)
                .replace("${trabajador}", trabajador)
                .replace("${fechaGeneracion}", LocalDateTime.now().format(DATE_FORMAT))
                .replace("${cantidadTrabajada}", String.valueOf(costos.size()))
                .replace("${resumenDias}", rows.toString())
                .replace("${totalMonto}", total != null ? total.setScale(2, RoundingMode.HALF_UP).toPlainString() : "0.00");
    }
}