package com.github.kraudy.InventoryBackend.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

/**
 * Request payload for bulk-creating productos.
 *
 * The cartesian product of {@code medidas} x {@code colores} is generated;
 * each resulting (medida, color) combination becomes a new Producto sharing
 * the same tipo / subTipo / modelo / nombre / descripcion and the same
 * precios and costos lists.
 *
 * Invalid combinations (those missing in producto_config) and combinations
 * that already exist are skipped and reported back in the response.
 */
@Data
public class ProductoBulkRequest {
    private String tipoProducto;
    private String subTipoProducto;
    private String modeloProducto;
    private List<String> medidas;
    private List<String> colores;

    private String nombre;
    private String descripcion;
    private Boolean activo;

    private List<PrecioItem> precios;
    private List<CostoItem> costos;

    @Data
    public static class PrecioItem {
        private BigDecimal precio;
        private String descripcion;
        private Integer cantidadRequerida;
    }

    @Data
    public static class CostoItem {
        private String tipoCosto;
        private BigDecimal costo;
        private String descripcion;
        private Integer cantidadRequerida;
    }
}
