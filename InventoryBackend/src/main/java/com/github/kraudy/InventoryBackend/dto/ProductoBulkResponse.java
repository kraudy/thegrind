package com.github.kraudy.InventoryBackend.dto;

import java.util.List;

import com.github.kraudy.InventoryBackend.model.Producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoBulkResponse {
    private List<Producto> created;
    private List<Skipped> skipped;
    private int totalRequested;
    private int totalCreated;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Skipped {
        private String medida;
        private String color;
        private String reason; // "invalid_combination" | "already_exists"
    }
}
