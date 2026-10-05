package com.example.auditoria.adapter.out.persistence;

import org.springframework.data.repository.Repository;

import java.util.List;

/**
 * Ajuste sobre la guía: allí extendía JpaRepository, que hereda deleteById,
 * deleteAll y saveAll sobre registros existentes. Extendiendo Repository solo
 * existen los dos métodos declarados: insertar y consultar. Es la forma de que
 * "append-only" sea verificable y no solo una convención.
 */
public interface HistorialCambioEstadoJpaRepository extends Repository<HistorialCambioEstadoJpaEntity, Long> {

    HistorialCambioEstadoJpaEntity save(HistorialCambioEstadoJpaEntity registro);

    /** Se ordena también por id: dos cambios en el mismo instante conservan su orden de inserción. */
    List<HistorialCambioEstadoJpaEntity> findByHallazgoIdOrderByFechaAscIdAsc(String hallazgoId);
}
