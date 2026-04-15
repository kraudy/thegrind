package com.github.kraudy.InventoryBackend.dto;

public record ProductoConfigDTO(
  String tipo,
  String subTipo,
  String medida,
  String modelo,
  String color
) {}
