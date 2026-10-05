package com.example.auditoria.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El dashboard se calcula con consultas GROUP BY sobre la misma tabla. Los hallazgos
 * se cierran hoy, así que los días de cierre dependen solo de la fecha de detección.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardEndpointTest {

    @Autowired
    private MockMvc mvc;

    private String registrar(String severidad, String area, int diasAtras) throws Exception {
        String cuerpo = mvc.perform(post("/api/hallazgos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Hallazgo de prueba","areaResponsable":"%s","severidad":"%s","fechaDeteccion":"%s"}
                                """.formatted(area, severidad, LocalDate.now().minusDays(diasAtras))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(cuerpo, "$.hallazgoId");
    }

    private void iniciar(String id) throws Exception {
        mvc.perform(patch("/api/hallazgos/{id}/iniciar-remediacion", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"responsable\":\"Responsable\",\"fechaLimite\":\"2026-12-31\"}"))
                .andExpect(status().isOk());
    }

    private void cerrar(String id) throws Exception {
        mvc.perform(patch("/api/hallazgos/{id}/cerrar", id)).andExpect(status().isOk());
    }

    @Test
    void cuentaPorSeveridadYEstadoConCerosYPromediaLosDiasDeCierrePorArea() throws Exception {
        String a = registrar("ALTA", "Infraestructura", 10);
        String b = registrar("ALTA", "Infraestructura", 20);
        registrar("BAJA", "Finanzas", 3);
        String d = registrar("CRITICA", "Finanzas", 6);
        iniciar(a);
        cerrar(a);
        iniciar(b);
        cerrar(b);
        iniciar(d);

        mvc.perform(get("/api/hallazgos/dashboard"))
                .andExpect(status().isOk())
                // Severidades en el orden del enum, incluida MEDIA con 0
                .andExpect(jsonPath("$.porSeveridad[0].categoria").value("CRITICA"))
                .andExpect(jsonPath("$.porSeveridad[0].total").value(1))
                .andExpect(jsonPath("$.porSeveridad[1].total").value(2))
                .andExpect(jsonPath("$.porSeveridad[2].categoria").value("MEDIA"))
                .andExpect(jsonPath("$.porSeveridad[2].total").value(0))
                .andExpect(jsonPath("$.porSeveridad[3].total").value(1))
                // Estados: 1 ABIERTO, 1 EN_REMEDIACION, 2 CERRADO, 0 REABIERTO
                .andExpect(jsonPath("$.porEstado[0].total").value(1))
                .andExpect(jsonPath("$.porEstado[1].total").value(1))
                .andExpect(jsonPath("$.porEstado[2].total").value(2))
                .andExpect(jsonPath("$.porEstado[3].total").value(0))
                // Solo Infraestructura tiene cerrados: (10 + 20) / 2 = 15 días
                .andExpect(jsonPath("$.promedioDiasCierrePorArea", hasSize(1)))
                .andExpect(jsonPath("$.promedioDiasCierrePorArea[0].categoria").value("Infraestructura"))
                .andExpect(jsonPath("$.promedioDiasCierrePorArea[0].promedioDias").value(15.0));
    }

    @Test
    void unHallazgoReabiertoSaleDelPromedioPorqueYaNoEstaCerrado() throws Exception {
        String a = registrar("MEDIA", "Seguridad", 4);
        String b = registrar("MEDIA", "Seguridad", 30);
        iniciar(a);
        cerrar(a);
        iniciar(b);
        cerrar(b);
        mvc.perform(patch("/api/hallazgos/{id}/reabrir", b).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Reaparecio\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/hallazgos/dashboard"))
                .andExpect(jsonPath("$.porEstado[3].categoria").value("REABIERTO"))
                .andExpect(jsonPath("$.porEstado[3].total").value(1))
                .andExpect(jsonPath("$.promedioDiasCierrePorArea[0].promedioDias").value(4.0));
    }
}
