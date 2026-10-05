package com.example.auditoria.adapter.in.web.dto;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.PlanRemediacion;

import java.time.LocalDate;

/** DTO del círculo Interface Adapters: el dominio nunca lo conoce. */
public record HallazgoResponse(
        String id,
        String titulo,
        String descripcion,
        String areaResponsable,
        String severidad,
        String estado,
        LocalDate fechaDeteccion,
        LocalDate fechaCierre,
        String planResponsable,
        LocalDate planFechaLimite,
        String planNotas) {

    public static HallazgoResponse desde(HallazgoAuditoria h) {
        PlanRemediacion plan = h.getPlanRemediacion();
        return new HallazgoResponse(
                h.getId().toString(), h.getTitulo(), h.getDescripcion(), h.getAreaResponsable(),
                h.getSeveridad().name(), h.getEstado().name(), h.getFechaDeteccion(), h.getFechaCierre(),
                plan == null ? null : plan.responsable(),
                plan == null ? null : plan.fechaLimite(),
                plan == null ? null : plan.notas());
    }
}
