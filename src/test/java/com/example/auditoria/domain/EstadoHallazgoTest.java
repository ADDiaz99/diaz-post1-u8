package com.example.auditoria.domain;

import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.example.auditoria.domain.valueobject.EstadoHallazgo.ABIERTO;
import static com.example.auditoria.domain.valueobject.EstadoHallazgo.CERRADO;
import static com.example.auditoria.domain.valueobject.EstadoHallazgo.EN_REMEDIACION;
import static com.example.auditoria.domain.valueobject.EstadoHallazgo.REABIERTO;
import static org.assertj.core.api.Assertions.assertThat;

class EstadoHallazgoTest {

    @Test
    void deLasDieciseisCombinacionesSoloCuatroSonValidas() {
        List<String> validas = new ArrayList<>();
        for (EstadoHallazgo origen : EstadoHallazgo.values()) {
            for (EstadoHallazgo destino : EstadoHallazgo.values()) {
                if (origen.puedeTransicionarA(destino)) {
                    validas.add(origen + "->" + destino);
                }
            }
        }
        assertThat(validas).containsExactlyInAnyOrder(
                "ABIERTO->EN_REMEDIACION", "EN_REMEDIACION->CERRADO",
                "CERRADO->REABIERTO", "REABIERTO->EN_REMEDIACION");
    }

    @Test
    void noSePuedeCerrarDesdeAbiertoNiReabrirDesdeAbierto() {
        assertThat(ABIERTO.puedeTransicionarA(CERRADO)).isFalse();
        assertThat(ABIERTO.puedeTransicionarA(REABIERTO)).isFalse();
        assertThat(EN_REMEDIACION.puedeTransicionarA(REABIERTO)).isFalse();
    }
}
