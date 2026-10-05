package com.example.auditoria.usecase;

import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.domain.valueobject.Severidad;
import com.example.auditoria.usecase.port.HistorialAuditoriaPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

/**
 * Prueba que el cambio de estado y su registro en la bitácora están en la MISMA
 * transacción: si la bitácora falla, el hallazgo no cambia de estado.
 *
 * No es @Transactional a propósito (cada caso de uso debe confirmar su propia
 * transacción) y usa su propia base H2 para no dejar datos a las demás pruebas.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:auditoria_transaccion")
class TransaccionBitacoraTest {

    @MockitoBean
    private HistorialAuditoriaPort historial;

    @Autowired
    private RegistrarHallazgoUseCase registrar;

    @Autowired
    private IniciarRemediacionUseCase iniciarRemediacion;

    @Autowired
    private CerrarHallazgoUseCase cerrar;

    @Autowired
    private ConsultarHallazgoUseCase consultar;

    @Test
    void siLaBitacoraFallaElCierreSeDeshace() {
        HallazgoId id = registrar.ejecutar("Backups sin probar", null, "Infraestructura", Severidad.CRITICA,
                LocalDate.of(2026, 8, 1), "auditora.ana");
        iniciarRemediacion.ejecutar(id, "Equipo de Infraestructura", LocalDate.of(2026, 12, 1), null, "jefe.infra");
        doThrow(new IllegalStateException("Bitacora no disponible"))
                .when(historial).registrar(any(), any(), eq(EstadoHallazgo.CERRADO), any(), any());

        assertThatThrownBy(() -> cerrar.ejecutar(id, "jefe.infra"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Bitacora no disponible");

        assertThat(consultar.buscarPorId(id).getEstado()).isEqualTo(EstadoHallazgo.EN_REMEDIACION);
        assertThat(consultar.buscarPorId(id).getFechaCierre()).isNull();
    }
}
