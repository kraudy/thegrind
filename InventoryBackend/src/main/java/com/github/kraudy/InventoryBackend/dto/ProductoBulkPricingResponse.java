package com.github.kraudy.InventoryBackend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoBulkPricingResponse {
    private int totalProductos;
    private int totalOperaciones;
    private int aplicadas;
    private int omitidas;
    private List<Skipped> skipped;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Skipped {
        private Long productoId;
        private String opType;
        private String detalle; // precio o tipoCosto que falló
        private String reason;  // not_found | already_exists | invalid
    }
}
