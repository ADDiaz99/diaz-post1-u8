package com.example.auditoria.usecase.impl;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.domain.valueobject.Severidad;
import com.example.auditoria.usecase.RegistrarHallazgoUseCase;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;
import com.example.auditoria.usecase.port.HistorialAuditoriaPort;

import java.time.LocalDate;

/** Sin anotaciones de Spring: el wiring y la transacción están en config/AuditoriaConfiguration. */
public class RegistrarHallazgoService implements RegistrarHallazgoUseCase {

    private final HallazgoRepositoryPort repo;
    private final HistorialAuditoriaPort historial;

    public RegistrarHallazgoService(HallazgoRepositoryPort repo, HistorialAuditoriaPort historial) {
        this.repo = repo;
        this.historial = historial;
    }

    @Override
    public HallazgoId ejecutar(String titulo, String descripcion, String areaResponsable,
                               Severidad severidad, LocalDate fechaDeteccion, String usuario) {
        HallazgoAuditoria hallazgo = new HallazgoAuditoria(
                HallazgoId.nuevo(), titulo, descripcion, areaResponsable, severidad, fechaDeteccion);
        repo.guardar(hallazgo);
        // La creación también queda en la bitácora: así el historial empieza en el origen del hallazgo.
        historial.registrar(hallazgo.getId(), null, hallazgo.getEstado(), "Registro del hallazgo", usuario);
        return hallazgo.getId();
    }
}
