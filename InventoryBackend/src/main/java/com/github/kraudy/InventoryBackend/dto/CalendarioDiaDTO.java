package com.github.kraudy.InventoryBackend.dto;

import java.time.LocalDate;
import java.util.List;

public record CalendarioDiaDTO(
    LocalDate date,
    String dayName,
    String relativeToToday,
    String weekLabel,
    int orderCount,
    List<OrdenCalendarioDTO> orders
) {}