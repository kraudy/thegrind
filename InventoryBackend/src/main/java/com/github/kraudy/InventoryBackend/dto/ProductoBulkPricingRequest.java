package com.github.kraudy.InventoryBackend.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

/**
 * Bulk-apply precio/costo operations to a set of existing productos.
 *
 * Every {@link Operation} in {@code operations} is applied to every producto
 * in {@code productoIds}, inside a single transaction. Failures per
 * (producto, operation) are reported in the response but do NOT roll back
 * the whole batch — successful operations are committed.
 */
@Data
public class ProductoBulkPricingRequest {
    private List<Long> productoIds;
    private List<Operation> operations;

    public enum OpType {
        ADD_PRECIO,
        REMOVE_PRECIO,
        UPSERT_COSTO,
        REMOVE_COSTO
    }

    @Data
    public static class Operation {
        private OpType type;

        // For ADD_PRECIO / REMOVE_PRECIO
        private BigDecimal precio;

        // For UPSERT_COSTO / REMOVE_COSTO
        private String tipoCosto;

        // For UPSERT_COSTO
        private BigDecimal costo;

        // Shared optional fields (used by ADD_PRECIO and UPSERT_COSTO)
        private String descripcion;
        private Integer cantidadRequerida;
    }
}
