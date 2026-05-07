package com.github.kraudy.InventoryBackend.model;


public enum OrdenEstadoEnum {
    RECIBIDA("Recibida"),
    REPARTIDA("Repartida"),
    LISTO("Listo"),
    ENTREGADO("Entregado"),
    FACTURADO("Facturado"),
    ;

    private final String dbValue;

    OrdenEstadoEnum(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String toString() {
        return dbValue;   // This is what gets saved to the DB
    }

    public static OrdenEstadoEnum fromString(String value) {
        for (OrdenEstadoEnum e : values()) {
            if (e.dbValue.equals(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado desconocido: " + value);
    }
}
