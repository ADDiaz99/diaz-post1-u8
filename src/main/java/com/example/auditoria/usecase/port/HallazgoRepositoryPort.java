package com.example.auditoria.usecase.port;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.HallazgoId;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: lo define el círculo Use Cases y lo implementa un Interface Adapter. */
public interface HallazgoRepositoryPort {

    void guardar(HallazgoAuditoria hallazgo);

    Optional<HallazgoAuditoria> buscarPorId(HallazgoId id);

    List<HallazgoAuditoria> buscarTodos();

    // Métodos añadidos en la Parte 2: mismo puerto, sin stack de lectura separado
    List<ConteoCategoria> contarPorSeveridad();

    List<ConteoCategoria> contarPorEstado();

    List<PromedioCategoria> promedioDiasCierrePorArea();
}
