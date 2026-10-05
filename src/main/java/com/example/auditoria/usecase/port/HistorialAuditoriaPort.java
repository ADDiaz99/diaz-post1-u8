package com.example.auditoria.usecase.port;

import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;

import java.util.List;

/**
 * Bitácora de cambios de estado: un solo puerto para escribir y leer. No es un
 * Event Store: el estado actual sigue viviendo en HallazgoRepositoryPort.
 *
 * Ajuste sobre la guía: se agrega "usuario", porque Cumplimiento exige saber
 * QUIÉN originó cada cambio y la firma de la guía no tenía dónde guardarlo.
 */
public interface HistorialAuditoriaPort {

    /** anterior es null cuando se registra la creación del hallazgo. */
    void registrar(HallazgoId hallazgoId, EstadoHallazgo anterior, EstadoHallazgo nuevo,
                   String motivo, String usuario);

    List<CambioEstadoView> listarPorHallazgo(HallazgoId hallazgoId);
}
