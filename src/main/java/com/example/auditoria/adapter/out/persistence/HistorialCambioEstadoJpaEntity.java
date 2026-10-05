package com.example.auditoria.adapter.out.persistence;

import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/**
 * Registro append-only de la bitácora. Tres barreras contra la modificación:
 * no hay setters (todo entra por el constructor), las columnas son
 * updatable = false, y @Immutable hace que Hibernate ignore cualquier UPDATE.
 */
@Entity
@Immutable
@Table(name = "historial_cambios_estado")
public class HistorialCambioEstadoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, length = 36)
    private String hallazgoId;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false, length = 20)
    private EstadoHallazgo estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private EstadoHallazgo estadoNuevo;

    @Column(updatable = false, length = 500)
    private String motivo;

    @Column(nullable = false, updatable = false, length = 100)
    private String usuario;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    /** Exigido por JPA. */
    protected HistorialCambioEstadoJpaEntity() {
    }

    public HistorialCambioEstadoJpaEntity(String hallazgoId, EstadoHallazgo estadoAnterior,
                                          EstadoHallazgo estadoNuevo, String motivo, String usuario,
                                          LocalDateTime fecha) {
        this.hallazgoId = hallazgoId;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.motivo = motivo;
        this.usuario = usuario;
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public String getHallazgoId() {
        return hallazgoId;
    }

    public EstadoHallazgo getEstadoAnterior() {
        return estadoAnterior;
    }

    public EstadoHallazgo getEstadoNuevo() {
        return estadoNuevo;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getUsuario() {
        return usuario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
