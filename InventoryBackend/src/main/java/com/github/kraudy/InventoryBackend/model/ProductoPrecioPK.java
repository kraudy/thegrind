package com.github.kraudy.InventoryBackend.model;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProductoPrecioPK implements Serializable {
    private Long productoId;
    
    private BigDecimal precio;
}
