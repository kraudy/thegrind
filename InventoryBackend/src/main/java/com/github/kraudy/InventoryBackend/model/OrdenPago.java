package com.github.kraudy.InventoryBackend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "OrdenPago")
public class OrdenPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_orden", nullable = false)
    private Long idOrden;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal monto;

    @CreationTimestamp
    @Column(updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime fechaPago;

    @Column(nullable = false, length = 30)
    private String metodoPago;        // Efectivo, Transferencia, Tarjeta, etc.

    @Column(length = 30)
    private String codigoReferencia;  // Código de referencia en caso de transferencia o tarjeta

    @Column(length = 20)
    private String banco;             // Banco en caso de transferencia o tarjeta. Bac, Lafise, Banpro, Otro

    @Column(nullable = false, length = 20)
    private String estado = "Pendiente";   // Pendiente, Aprobado

    @Column(length = 50)
    private String recibidoPor;

    @Column(length = 50)
    private String aprobadoPor;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime fechaAprobacion;

    @Column(columnDefinition = "TEXT")
    private String notas;

    // Relationship to Orden
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden", insertable = false, updatable = false)
    private Orden orden;
}