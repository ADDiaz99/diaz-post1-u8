package com.example.auditoria.adapter.in.web.dto;

import com.example.auditoria.domain.valueobject.Severidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record RegistrarHallazgoRequest(
        @NotBlank(message = "es obligatorio") String titulo,
        String descripcion,
        @NotBlank(message = "es obligatoria") String areaResponsable,
        @NotNull(message = "es obligatoria") Severidad severidad,
        @NotNull(message = "es obligatoria")
        @PastOrPresent(message = "no puede ser futura") LocalDate fechaDeteccion) {
}
