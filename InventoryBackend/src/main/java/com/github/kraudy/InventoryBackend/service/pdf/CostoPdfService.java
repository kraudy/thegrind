package com.github.kraudy.InventoryBackend.service.pdf;

import com.github.kraudy.InventoryBackend.dto.OrdenCostoDTO;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimientoPK;
import com.github.kraudy.InventoryBackend.repository.OrdenSeguimientoRepository;
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
import java.util.Objects;
import java.util.TreeMap;
import java.util.List;

@Service
public class CostoPdfService {

    private final OrdenSeguimientoRepository ordenSeguimientoRepository;

    public CostoPdfService(OrdenSeguimientoRepository ordenSeguimientoRepository) {
        this.ordenSeguimientoRepository = ordenSeguimientoRepository;
    }

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DIA_NOMBRE_FORMAT = DateTimeFormatter.ofPattern("EEEE", Locale.forLanguageTag("es-NI")); // Lunes, Martes...

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
            </style>
        </head>
        <body>
            <h1>RECIBO DE PAGO</h1>
            <div class="subtitle">Tipo: <strong>${tipoCosto}</strong> • Trabajador: <strong>${trabajador}</strong></div>

            <table>
                <thead>
                    <tr>
                        <th>Fecha Trabajo</th>
                        <th>Día</th>
                        <th>Cantidad Trabajada</th>
                        <th>Costo (C$)</th>
                        <th>Sub Total (C$)</th>
                    </tr>
                </thead>
                <tbody>
                    ${resumenDias}
                </tbody>
            </table>

            <div class="signature" style="margin-top: 50px;">
                <div class="signature-line"></div>
                <p style="margin: 0;"><strong>Firma del Trabajador</strong></p>
                <p style="margin: 4px 0 0 0;">${trabajador}</p>
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
        Map<LocalDate, Integer> dailySpecialCounts = new TreeMap<>(); // Caritas (Reparacion) o Calados (Pegado)
        Map<LocalDate, Integer> dailyOtherCounts = new TreeMap<>();

        Map<String, Boolean> specialByDetalle = new TreeMap<>();

        Integer totalCantidad = 0;
        Integer totalCantidadSpecial = 0;
        Integer totalCantidadOther = 0;

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

                boolean isSpecial = isSpecialDetalle(costo, tipoCosto, specialByDetalle);
                totalCantidad += cantidad; // Acumulamos total cantidad para el resumen general

                if (isSpecial) {
                    totalCantidadSpecial += cantidad;
                    dailySpecialCounts.merge(fecha, cantidad, Integer::sum);
                } else {
                    totalCantidadOther += cantidad;
                    dailyOtherCounts.merge(fecha, cantidad, Integer::sum);
                }

                dailyTotals.merge(fecha, subTotal, BigDecimal::add);
                dailyCounts.merge(fecha, cantidad, Integer::sum);
            }
        }

        LocalDate baseDate = costos.stream()
                .map(OrdenCostoDTO::fechaTrabajo)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());
        LocalDate monday = baseDate.minusDays(baseDate.getDayOfWeek().getValue() - 1L);

        BigDecimal costoUnitario = costos.stream()
                .map(OrdenCostoDTO::costo)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(BigDecimal.ZERO);

        BigDecimal costoSpecial = costos.stream()
                .filter(c -> isSpecialDetalle(c, tipoCosto, specialByDetalle))
                .map(OrdenCostoDTO::costo)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(BigDecimal.ZERO);

        BigDecimal costoOther = costos.stream()
                .filter(c -> !isSpecialDetalle(c, tipoCosto, specialByDetalle))
                .map(OrdenCostoDTO::costo)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(BigDecimal.ZERO);

        final boolean splitByProductType = "Reparacion".equalsIgnoreCase(tipoCosto) || "Pegado".equalsIgnoreCase(tipoCosto);
        final String specialLabel = "Reparacion".equalsIgnoreCase(tipoCosto) ? "Caritas" : "Calados";

        // Construir filas del resumen (Lunes a Sabado, con ceros si no hay datos)
        StringBuilder rows = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            LocalDate date = monday.plusDays(i);
            String diaNombre = date.format(DIA_NOMBRE_FORMAT);
            int count = dailyCounts.getOrDefault(date, 0);
            int specialCount = dailySpecialCounts.getOrDefault(date, 0);
            int otherCount = dailyOtherCounts.getOrDefault(date, 0);
            BigDecimal dailyTotal = dailyTotals.getOrDefault(date, BigDecimal.ZERO);

            String cantidadCell = splitByProductType
                    ? String.format("%d<br/><span style=\"font-size:12px; color:#475569;\">%s: %d | Otros: %d</span>",
                        count, specialLabel, specialCount, otherCount)
                    : String.valueOf(count);

            String costoCell = splitByProductType
                    ? String.format("<div style=\"font-weight:normal; font-size:12px; color:#475569;\">%s: %s</div><div style=\"font-weight:normal; font-size:12px; color:#475569;\">Otros: %s</div>",
                        specialLabel,
                        costoSpecial.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                        costoOther.setScale(2, RoundingMode.HALF_UP).toPlainString())
                    : costoUnitario.setScale(2, RoundingMode.HALF_UP).toPlainString();

            rows.append(String.format("""
                <tr>
                    <td style="text-align:center;">%s</td>
                    <td>%s</td>
                    <td style="text-align:center;">%s</td>
                    <td style="text-align:right; font-weight:bold;">%s</td>
                    <td style="text-align:right; font-weight:bold;">%.2f</td>
                </tr>
                """,
                date.format(DATE_ONLY_FORMAT),
                diaNombre,
                cantidadCell,
                costoCell,
                dailyTotal
            ));
        }

        String totalCantidadCell = splitByProductType
                ? String.format("%d<br/><span style=\"font-size:12px; color:#475569;\">%s: %d | Otros: %d</span>",
                    totalCantidad, specialLabel, totalCantidadSpecial, totalCantidadOther)
                : String.valueOf(totalCantidad);

        rows.append(String.format("""
            <tr>
                <td colspan="2" style="text-align:right; font-weight:bold;">TOTAL</td>
                <td style="text-align:center; font-weight:bold;">%s</td>
                <td style="text-align:center; font-weight:bold;">-</td>
                <td style="text-align:right; font-weight:bold;">%s</td>
            </tr>
            """,
            totalCantidadCell,
            total != null ? total.setScale(2, RoundingMode.HALF_UP).toPlainString() : "0.00"
        ));

        return PDF_TEMPLATE
                .replace("${tipoCosto}", tipoCosto)
                .replace("${trabajador}", trabajador)
                .replace("${fechaGeneracion}", LocalDateTime.now().format(DATE_FORMAT))
            .replace("${resumenDias}", rows.toString());
    }

    private boolean isSpecialDetalle(OrdenCostoDTO costo, String tipoCosto, Map<String, Boolean> cache) {
        if (costo == null || costo.idOrden() == null || costo.idOrdenDetalle() == null) {
            return false;
        }

        if (!"Reparacion".equalsIgnoreCase(tipoCosto) && !"Pegado".equalsIgnoreCase(tipoCosto)) {
            return false;
        }

        String cacheKey = costo.idOrden() + "-" + costo.idOrdenDetalle() + "-" + tipoCosto;
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        String specialType = "Reparacion".equalsIgnoreCase(tipoCosto) ? "Carita" : "Calado";

        boolean isSpecial = ordenSeguimientoRepository
                .findById(new OrdenSeguimientoPK(costo.idOrden(), costo.idOrdenDetalle()))
                .map(seg -> seg.getTipo() != null && specialType.equalsIgnoreCase(seg.getTipo()))
                .orElse(false);

        cache.put(cacheKey, isSpecial);
        return isSpecial;
    }
}
