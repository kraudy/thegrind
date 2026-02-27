package com.github.kraudy.InventoryBackend.dto;

import java.time.Duration;
import java.time.LocalDateTime;

public record OrdenSeguimientoDTO(
    Long id,
    Long idCliente,
    String clienteNombre,
    String creadaPor,
    LocalDateTime fechaVencimiento,
    String tiempoRestante
) {}