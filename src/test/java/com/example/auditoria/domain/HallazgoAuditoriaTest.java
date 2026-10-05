package com.example.auditoria.domain;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.domain.valueobject.PlanRemediacion;
import com.example.auditoria.domain.valueobject.Severidad;
import com.example.auditoria.domain.valueobject.TransicionInvalidaException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Prueba del Aggregate Root sin @SpringBootTest: el círculo Entities no necesita Spring. */
class HallazgoAuditoriaTest {

    private static final LocalDate DETECCION = LocalDate.of(2026, 8, 1);

    private HallazgoAuditoria nuevo() {
        return new HallazgoAuditoria(HallazgoId.nuevo(), "Credenciales por defecto", "Servidor QA",
                "Infraestructura", Severidad.ALTA, DETECCION);
    }

    private PlanRemediacion plan() {
        return new PlanRemediacion("Equipo de Infraestructura", LocalDate.of(2026, 8, 20), "Rotar credenciales");
    }

    @Test
    void unHallazgoNuevoQuedaAbiertoSinPlanNiFechaDeCierre() {
        HallazgoAuditoria hallazgo = nuevo();

        assertThat(hallazgo.getEstado()).isEqualTo(EstadoHallazgo.ABIERTO);
        assertThat(hallazgo.getPlanRemediacion()).isNull();
        assertThat(hallazgo.getFechaCierre()).isNull();
    }

    @Test
    void recorreElCicloCompletoYCadaTransicionDevuelveElEstadoAnterior() {
        HallazgoAuditoria hallazgo = nuevo();

        assertThat(hallazgo.iniciarRemediacion(plan())).isEqualTo(EstadoHallazgo.ABIERTO);
        assertThat(hallazgo.cerrar()).isEqualTo(EstadoHallazgo.EN_REMEDIACION);
        assertThat(hallazgo.getFechaCierre()).isEqualTo(LocalDate.now());
        assertThat(hallazgo.reabrir()).isEqualTo(EstadoHallazgo.CERRADO);
        assertThat(hallazgo.getFechaCierre()).isNull();
        assertThat(hallazgo.iniciarRemediacion(plan())).isEqualTo(EstadoHallazgo.REABIERTO);
        assertThat(hallazgo.getEstado()).isEqualTo(EstadoHallazgo.EN_REMEDIACION);
    }

    @Test
    void noSePuedeCerrarSinPlanDeRemediacion() {
        HallazgoAuditoria hallazgo = nuevo();

        assertThatThrownBy(hallazgo::cerrar)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sin plan de remediacion");
        assertThat(hallazgo.getEstado()).isEqualTo(EstadoHallazgo.ABIERTO);
    }

    @Test
    void noSePuedeReabrirUnHallazgoQueSigueAbierto() {
        HallazgoAuditoria hallazgo = nuevo();

        assertThatThrownBy(hallazgo::reabrir)
                .isInstanceOf(TransicionInvalidaException.class)
                .hasMessage("No se puede transicionar de ABIERTO a REABIERTO");
    }

    @Test
    void unPlanNuloNoDejaElHallazgoEnRemediacion() {
        HallazgoAuditoria hallazgo = nuevo();

        assertThatThrownBy(() -> hallazgo.iniciarRemediacion(null))
                .isInstanceOf(NullPointerException.class);
        assertThat(hallazgo.getEstado()).isEqualTo(EstadoHallazgo.ABIERTO);
    }

    @Test
    void elTituloYElAreaSonObligatorios() {
        assertThatThrownBy(() -> new HallazgoAuditoria(HallazgoId.nuevo(), " ", null, "TI", Severidad.BAJA, DETECCION))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new HallazgoAuditoria(HallazgoId.nuevo(), "Titulo", null, "", Severidad.BAJA, DETECCION))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void elPlanExigeResponsable() {
        assertThatThrownBy(() -> new PlanRemediacion("", LocalDate.of(2026, 9, 1), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reconstituirConservaUnHallazgoReabiertoYSuFechaDeCierreOriginal() {
        HallazgoId id = HallazgoId.nuevo();
        LocalDate cierreOriginal = LocalDate.of(2026, 8, 15);

        HallazgoAuditoria reabierto = HallazgoAuditoria.reconstituir(id, "Titulo", null, "TI", Severidad.MEDIA,
                DETECCION, EstadoHallazgo.REABIERTO, plan(), null);
        HallazgoAuditoria cerrado = HallazgoAuditoria.reconstituir(id, "Titulo", null, "TI", Severidad.MEDIA,
                DETECCION, EstadoHallazgo.CERRADO, plan(), cierreOriginal);

        assertThat(reabierto.getEstado()).isEqualTo(EstadoHallazgo.REABIERTO);
        assertThat(cerrado.getFechaCierre()).isEqualTo(cierreOriginal);
    }
}
