package com.github.kraudy.InventoryBackend.model;

public enum EstadoSeguimientoEnum {
    RECIBIDA("Recibida"),
    REPARTIDA("Repartida"),
    REPARACION("Reparacion"),
    IMPRESION("Impresion"),
    ENMARCADO("Enmarcado"),
    PEGADO("Pegado"),
    LISTO("Listo"),
    ENTREGADO("Entregado"),
    NORMAL("Normal"),
    // Nuevos estados aquí
    BODEGA("Bodega"),
    ARMADO("Armado"),
    SUBLIMACION("Sublimacion"),
    CALADO("Calado")
    ;

    private final String dbValue;

    EstadoSeguimientoEnum(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String toString() {
        return dbValue;   // This is what gets saved to the DB
    }

    public static EstadoSeguimientoEnum fromString(String value) {
        for (EstadoSeguimientoEnum e : values()) {
            if (e.dbValue.equals(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado desconocido: " + value);
    }
}
