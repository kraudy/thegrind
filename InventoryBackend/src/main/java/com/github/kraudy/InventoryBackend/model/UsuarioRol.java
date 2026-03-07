package com.github.kraudy.InventoryBackend.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)  // good practice for natural keys
@Table(name = "UsuarioRol")
public class UsuarioRol {
  @Id
  @EqualsAndHashCode.Include
  @Column(nullable = false, columnDefinition = "VARCHAR(50)")
  private String usuario;

  @Id
  @EqualsAndHashCode.Include
  @Column(nullable = false, columnDefinition = "VARCHAR(30)")
  private String rol;

  /* Relations */
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario", nullable = false, insertable = false, updatable = false)
  private Usuario usuarioEntity;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rol", nullable = false, insertable = false, updatable = false)
  private Rol rolEntity;
}
