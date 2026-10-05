package com.example.auditoria.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Extendido en la Parte 2 con consultas agregadas sobre la MISMA tabla: no hay
 * base de datos ni esquema de lectura aparte.
 */
public interface HallazgoJpaRepository extends JpaRepository<HallazgoJpaEntity, String> {

    @Query("SELECT h.severidad AS categoria, COUNT(h) AS total FROM HallazgoJpaEntity h GROUP BY h.severidad")
    List<ConteoProjection> contarPorSeveridad();

    @Query("SELECT h.estado AS categoria, COUNT(h) AS total FROM HallazgoJpaEntity h GROUP BY h.estado")
    List<ConteoProjection> contarPorEstado();

    /**
     * Ajuste sobre la guía: en vez de DATEDIFF('DAY', ...), que HQL pasa tal cual a
     * la base y solo funciona en motores con esa función, se usa la aritmética de
     * fechas de HQL: (fin - inicio) BY DAY. Hibernate la traduce al dialecto activo
     * (en H2, datediff(day, ...)), así la consulta sobrevive a un cambio de base.
     */
    @Query("SELECT h.areaResponsable AS categoria, "
            + "AVG((h.fechaCierre - h.fechaDeteccion) BY DAY) AS promedio "
            + "FROM HallazgoJpaEntity h "
            + "WHERE h.estado = com.example.auditoria.domain.valueobject.EstadoHallazgo.CERRADO "
            + "GROUP BY h.areaResponsable")
    List<PromedioProjection> promedioDiasCierrePorArea();

    interface ConteoProjection {
        String getCategoria();

        Long getTotal();
    }

    interface PromedioProjection {
        String getCategoria();

        Double getPromedio();
    }
}
