package com.github.kraudy.InventoryBackend.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class EstadosPorDetalleDTO {

    private Long idOrdenDetalle;
    private List<String> estados;
    private String estadoActual;

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Basicamente estamos convirtiendo los estados a JSON y luego a una lista
     * que despues de vuelve a pasar a JSON al devolverla
     */
    public EstadosPorDetalleDTO(Long idOrdenDetalle, String estadosJson, String estadoActual) {
        this.idOrdenDetalle = idOrdenDetalle;
        this.estados = parseJson(estadosJson);
        this.estadoActual = estadoActual;
    }

    private List<String> parseJson(String json) {
        if (json == null || json.trim().isEmpty() || "null".equals(json)) {
            return List.of();
        }
        try {
            return mapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // fallback — never breaks the endpoint
            e.printStackTrace();
            return List.of();
        }
    }
}