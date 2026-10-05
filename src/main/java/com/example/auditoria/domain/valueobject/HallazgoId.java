package com.example.auditoria.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/** Identidad tipada: un HallazgoId no se puede confundir con cualquier otro UUID o String. */
public record HallazgoId(UUID valor) {

    public HallazgoId {
        Objects.requireNonNull(valor, "HallazgoId no puede ser nulo");
    }

    public static HallazgoId nuevo() {
        return new HallazgoId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
