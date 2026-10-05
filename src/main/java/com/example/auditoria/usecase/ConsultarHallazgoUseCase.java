package com.example.auditoria.usecase;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.HallazgoId;

import java.util.List;

/**
 * Ajuste sobre la guía: allí este caso de uso devolvía HallazgoResponse, un DTO del
 * paquete adapter/in/web. Eso hace que el círculo Use Cases dependa de un círculo
 * externo y rompe la Dependency Rule. Aquí devuelve el agregado y el controlador
 * lo convierte a DTO.
 */
public interface ConsultarHallazgoUseCase {

    HallazgoAuditoria buscarPorId(HallazgoId id);

    List<HallazgoAuditoria> listarTodos();
}
