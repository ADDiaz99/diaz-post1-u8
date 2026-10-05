package com.example.auditoria.usecase.impl;

import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.usecase.ConsultarHistorialUseCase;
import com.example.auditoria.usecase.HallazgoNotFoundException;
import com.example.auditoria.usecase.port.CambioEstadoView;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;
import com.example.auditoria.usecase.port.HistorialAuditoriaPort;

import java.util.List;

public class ConsultarHistorialService implements ConsultarHistorialUseCase {

    private final HallazgoRepositoryPort repo;
    private final HistorialAuditoriaPort historial;

    public ConsultarHistorialService(HallazgoRepositoryPort repo, HistorialAuditoriaPort historial) {
        this.repo = repo;
        this.historial = historial;
    }

    /** 404 si el hallazgo no existe, en vez de una lista vacía que parecería "sin cambios". */
    @Override
    public List<CambioEstadoView> ejecutar(HallazgoId id) {
        if (repo.buscarPorId(id).isEmpty()) {
            throw new HallazgoNotFoundException(id);
        }
        return historial.listarPorHallazgo(id);
    }
}
