package com.github.kraudy.InventoryBackend.model;

import java.time.LocalDateTime;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Data
@Table(name = "OrdenCalendarioHistorico")
public class OrdenCalendarioHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long idOrden;                    // original order id

    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime fechaTrabajoOriginal;

    @Column(nullable = false, columnDefinition = "DATE")
    private LocalDate fechaOriginal;

    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime fechaArchivado;

    @Column(nullable = false, columnDefinition = "VARCHAR(100)")
    private String usuarioArchivado = "SYSTEM";

    // You can add more fields later (e.g. motivo, cantidadDetalles, etc.)
}