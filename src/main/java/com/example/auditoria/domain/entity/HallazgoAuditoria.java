package com.example.auditoria.domain.entity;

import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.domain.valueobject.PlanRemediacion;
import com.example.auditoria.domain.valueobject.Severidad;
import com.example.auditoria.domain.valueobject.TransicionInvalidaException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Aggregate Root del círculo Entities. Java puro: sin Spring ni JPA.
 * El estado solo cambia por los métodos de dominio (no hay setters), y cada
 * transición pasa por la máquina de estados de EstadoHallazgo.
 */
public class HallazgoAuditoria {

    private final HallazgoId id;
    private final String titulo;
    private final String descripcion;
    private final String areaResponsable;
    private final Severidad severidad;
    private final LocalDate fechaDeteccion;
    private EstadoHallazgo estado;
    private PlanRemediacion planRemediacion;
    private LocalDate fechaCierre;

    public HallazgoAuditoria(HallazgoId id, String titulo, String descripcion,
                             String areaResponsable, Severidad severidad, LocalDate fechaDeteccion) {
        Objects.requireNonNull(id, "El id es obligatorio");
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El titulo es obligatorio");
        }
        if (areaResponsable == null || areaResponsable.isBlank()) {
            throw new IllegalArgumentException("El area responsable es obligatoria");
        }
        // Ajuste sobre la guía: severidad y fecha de detección también son obligatorias;
        // sin ellas el hallazgo no se puede clasificar ni medir en el dashboard.
        Objects.requireNonNull(severidad, "La severidad es obligatoria");
        Objects.requireNonNull(fechaDeteccion, "La fecha de deteccion es obligatoria");
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.areaResponsable = areaResponsable;
        this.severidad = severidad;
        this.fechaDeteccion = fechaDeteccion;
        this.estado = EstadoHallazgo.ABIERTO;
    }

    /**
     * Reconstruye un hallazgo ya persistido con el estado que tenía, sin volver a
     * ejecutar las transiciones. Solo lo usa el adaptador de persistencia.
     *
     * Ajuste sobre la guía: su toDomain() repetía iniciarRemediacion() y cerrar()
     * para llegar al estado guardado. Eso fallaba con un hallazgo REABIERTO
     * (EN_REMEDIACION no puede pasar a REABIERTO) y cambiaba la fecha de cierre
     * por la fecha del día de la consulta.
     */
    public static HallazgoAuditoria reconstituir(HallazgoId id, String titulo, String descripcion,
                                                 String areaResponsable, Severidad severidad,
                                                 LocalDate fechaDeteccion, EstadoHallazgo estado,
                                                 PlanRemediacion planRemediacion, LocalDate fechaCierre) {
        HallazgoAuditoria hallazgo = new HallazgoAuditoria(id, titulo, descripcion, areaResponsable,
                severidad, fechaDeteccion);
        Objects.requireNonNull(estado, "El estado persistido es obligatorio");
        if (estado != EstadoHallazgo.ABIERTO && planRemediacion == null) {
            throw new IllegalStateException("Hallazgo " + id + " en estado " + estado + " sin plan de remediacion");
        }
        if (estado == EstadoHallazgo.CERRADO && fechaCierre == null) {
            throw new IllegalStateException("Hallazgo " + id + " CERRADO sin fecha de cierre");
        }
        hallazgo.estado = estado;
        hallazgo.planRemediacion = planRemediacion;
        hallazgo.fechaCierre = fechaCierre;
        return hallazgo;
    }

    public EstadoHallazgo iniciarRemediacion(PlanRemediacion plan) {
        // Ajuste sobre la guía: se valida el plan ANTES de transicionar. En la guía, un
        // plan nulo dejaba el hallazgo en EN_REMEDIACION sin plan y luego lanzaba la excepción.
        Objects.requireNonNull(plan, "El plan de remediacion es obligatorio");
        EstadoHallazgo anterior = transicionar(EstadoHallazgo.EN_REMEDIACION);
        this.planRemediacion = plan;
        return anterior;
    }

    public EstadoHallazgo cerrar() {
        if (planRemediacion == null) {
            throw new IllegalStateException("No se puede cerrar un hallazgo sin plan de remediacion");
        }
        EstadoHallazgo anterior = transicionar(EstadoHallazgo.CERRADO);
        this.fechaCierre = LocalDate.now();
        return anterior;
    }

    public EstadoHallazgo reabrir() {
        EstadoHallazgo anterior = transicionar(EstadoHallazgo.REABIERTO);
        this.fechaCierre = null;
        return anterior;
    }

    private EstadoHallazgo transicionar(EstadoHallazgo destino) {
        if (!estado.puedeTransicionarA(destino)) {
            throw new TransicionInvalidaException(estado, destino);
        }
        EstadoHallazgo anterior = this.estado;
        this.estado = destino;
        return anterior;
    }

    public HallazgoId getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getAreaResponsable() {
        return areaResponsable;
    }

    public Severidad getSeveridad() {
        return severidad;
    }

    public LocalDate getFechaDeteccion() {
        return fechaDeteccion;
    }

    public EstadoHallazgo getEstado() {
        return estado;
    }

    public PlanRemediacion getPlanRemediacion() {
        return planRemediacion;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }
}
