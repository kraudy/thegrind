package com.github.kraudy.InventoryBackend.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.github.kraudy.InventoryBackend.model.Producto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Wrapper DTO returned by {@code GET /api/productos} so the catalog list can
 * show at-a-glance pricing and cost information for each product without
 * issuing one HTTP call per row.
 *
 * <p>The underlying {@link Producto} is serialized flat via {@link JsonUnwrapped}
 * to preserve backwards compatibility with the existing frontend shape; this
 * DTO just adds {@code precios} and {@code costos} arrays.
 */
@Data
@AllArgsConstructor
public class ProductoListDTO {

    @JsonUnwrapped
    private Producto producto;

    private List<PrecioMini> precios;

    private List<CostoMini> costos;

    @Data
    @AllArgsConstructor
    public static class PrecioMini {
        private BigDecimal precio;
        private String descripcion;
        private int cantidadRequerida;
    }

    @Data
    @AllArgsConstructor
    public static class CostoMini {
        private String tipoCosto;
        private BigDecimal costo;
    }
}
