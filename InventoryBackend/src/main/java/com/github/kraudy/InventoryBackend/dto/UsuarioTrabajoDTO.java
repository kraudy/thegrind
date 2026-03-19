package com.github.kraudy.InventoryBackend.dto;


public record UsuarioTrabajoDTO(
  String usuario,
  Long cantidadAsignada,
  Long cantidadTrabajada
) {}

