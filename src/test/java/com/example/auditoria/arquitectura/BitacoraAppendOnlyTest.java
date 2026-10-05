package com.example.auditoria.arquitectura;

import com.example.auditoria.adapter.out.persistence.HistorialCambioEstadoJpaEntity;
import com.example.auditoria.adapter.out.persistence.HistorialCambioEstadoJpaRepository;
import com.example.auditoria.usecase.port.HistorialAuditoriaPort;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.CrudRepository;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/** Checkpoint "HistorialCambioEstado es append-only", verificado por reflexión y no por convención. */
class BitacoraAppendOnlyTest {

    @Test
    void elRepositorioDeLaBitacoraSoloSabeInsertarYConsultar() {
        assertThat(CrudRepository.class.isAssignableFrom(HistorialCambioEstadoJpaRepository.class)).isFalse();
        assertThat(Arrays.stream(HistorialCambioEstadoJpaRepository.class.getMethods()).map(Method::getName))
                .allMatch(nombre -> nombre.equals("save") || nombre.startsWith("find"));
    }

    @Test
    void laEntidadDeLaBitacoraNoTieneSetters() {
        assertThat(Arrays.stream(HistorialCambioEstadoJpaEntity.class.getDeclaredMethods()).map(Method::getName))
                .noneMatch(nombre -> nombre.startsWith("set"));
    }

    @Test
    void elPuertoSoloRegistraYLista() {
        assertThat(Arrays.stream(HistorialAuditoriaPort.class.getDeclaredMethods()).map(Method::getName))
                .containsExactlyInAnyOrder("registrar", "listarPorHallazgo");
    }
}
