package com.example.auditoria.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Checkpoints del Paso 11 sobre la bitácora: orden cronológico, un registro por transición exitosa. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HistorialEndpointTest {

    @Autowired
    private MockMvc mvc;

    private String registrar(String usuario) throws Exception {
        var peticion = post("/api/hallazgos").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"titulo":"Credenciales por defecto","areaResponsable":"Infraestructura",
                         "severidad":"ALTA","fechaDeteccion":"2026-08-01"}
                        """);
        if (usuario != null) {
            peticion.header("X-Usuario", usuario);
        }
        String cuerpo = mvc.perform(peticion).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(cuerpo, "$.hallazgoId");
    }

    @Test
    void elCicloCompletoQuedaEnOrdenConQuienOriginoCadaCambio() throws Exception {
        String id = registrar("auditora.ana");
        mvc.perform(patch("/api/hallazgos/{id}/iniciar-remediacion", id).header("X-Usuario", "jefe.infra")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"responsable\":\"Equipo de Infraestructura\",\"fechaLimite\":\"2026-12-20\"}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/hallazgos/{id}/cerrar", id).header("X-Usuario", "jefe.infra"))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/hallazgos/{id}/reabrir", id).header("X-Usuario", "auditora.ana")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Reaparecio en el seguimiento\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/hallazgos/{id}/historial", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].estadoAnterior").doesNotExist())
                .andExpect(jsonPath("$[0].estadoNuevo").value("ABIERTO"))
                .andExpect(jsonPath("$[0].usuario").value("auditora.ana"))
                .andExpect(jsonPath("$[1].estadoAnterior").value("ABIERTO"))
                .andExpect(jsonPath("$[1].estadoNuevo").value("EN_REMEDIACION"))
                .andExpect(jsonPath("$[1].usuario").value("jefe.infra"))
                .andExpect(jsonPath("$[2].estadoNuevo").value("CERRADO"))
                .andExpect(jsonPath("$[3].estadoAnterior").value("CERRADO"))
                .andExpect(jsonPath("$[3].estadoNuevo").value("REABIERTO"))
                .andExpect(jsonPath("$[3].motivo").value("Reaparecio en el seguimiento"));
    }

    @Test
    void unaTransicionRechazadaNoInsertaNingunRegistro() throws Exception {
        String id = registrar("auditora.ana");

        mvc.perform(patch("/api/hallazgos/{id}/cerrar", id)).andExpect(status().isBadRequest());

        mvc.perform(get("/api/hallazgos/{id}/historial", id))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].estadoNuevo").value("ABIERTO"));
    }

    @Test
    void sinCabeceraElCambioQuedaAComoAnonimo() throws Exception {
        String id = registrar(null);

        mvc.perform(get("/api/hallazgos/{id}/historial", id))
                .andExpect(jsonPath("$[0].usuario").value("anonimo"));
    }

    @Test
    void elHistorialDeUnHallazgoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/hallazgos/{id}/historial", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }
}
