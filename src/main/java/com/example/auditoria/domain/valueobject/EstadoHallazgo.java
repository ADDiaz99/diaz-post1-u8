package com.example.auditoria.domain.valueobject;

/**
 * Enum con comportamiento: encapsula la máquina de estados del hallazgo.
 * Transiciones válidas: ABIERTO -> EN_REMEDIACION -> CERRADO -> REABIERTO -> EN_REMEDIACION.
 */
public enum EstadoHallazgo {
    ABIERTO, EN_REMEDIACION, CERRADO, REABIERTO;

    public boolean puedeTransicionarA(EstadoHallazgo destino) {
        return switch (this) {
            case ABIERTO -> destino == EN_REMEDIACION;
            case EN_REMEDIACION -> destino == CERRADO;
            case CERRADO -> destino == REABIERTO;
            case REABIERTO -> destino == EN_REMEDIACION;
        };
    }
}
