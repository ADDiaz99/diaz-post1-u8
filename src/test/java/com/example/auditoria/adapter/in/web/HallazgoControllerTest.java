package com.example.auditoria.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Checkpoints del Paso 6 contra la API real con H2. Cada prueba se deshace al terminar. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HallazgoControllerTest {

    private static final String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";

    @Autowired
    private MockMvc mvc;

    private String registrar(String titulo, String area, String severidad) throws Exception {
        String json = """
                {"titulo":"%s","descripcion":"Detectado en la revision","areaResponsable":"%s",
                 "severidad":"%s","fechaDeteccion":"%s"}
                """.formatted(titulo, area, severidad, LocalDate.now().minusDays(5));
        MvcResult resultado = mvc.perform(post("/api/hallazgos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.hallazgoId");
    }

    private void iniciarRemediacion(String id) throws Exception {
        mvc.perform(patch("/api/hallazgos/{id}/iniciar-remediacion", id).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"responsable":"Equipo de Infraestructura","fechaLimite":"2026-12-20","notas":"Rotar credenciales"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void registrarRetorna201ConUnHallazgoIdEnFormatoUuid() throws Exception {
        mvc.perform(post("/api/hallazgos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Credenciales por defecto","descripcion":"Servidor QA",
                                 "areaResponsable":"Infraestructura","severidad":"ALTA","fechaDeteccion":"2026-08-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hallazgoId", matchesPattern(UUID_REGEX)));
    }

    @Test
    void iniciarRemediacionSobreUnHallazgoAbiertoLoPasaAEnRemediacion() throws Exception {
        String id = registrar("Puerto abierto", "Redes", "MEDIA");

        iniciarRemediacion(id);

        mvc.perform(get("/api/hallazgos/{id}", id))
                .andExpect(jsonPath("$.estado").value("EN_REMEDIACION"))
                .andExpect(jsonPath("$.planResponsable").value("Equipo de Infraestructura"));
    }

    @Test
    void cerrarUnHallazgoAbiertoRetorna400() throws Exception {
        String id = registrar("Logs sin rotacion", "Operaciones", "BAJA");

        mvc.perform(patch("/api/hallazgos/{id}/cerrar", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipo").value("IllegalStateException"));
    }

    @Test
    void reabrirUnHallazgoCerradoRetorna200YQuedaReabiertoSinFechaDeCierre() throws Exception {
        String id = registrar("Backups sin probar", "Infraestructura", "CRITICA");
        iniciarRemediacion(id);
        mvc.perform(patch("/api/hallazgos/{id}/cerrar", id)).andExpect(status().isOk());

        mvc.perform(patch("/api/hallazgos/{id}/reabrir", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"El hallazgo reaparecio en la auditoria de seguimiento\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REABIERTO"));

        // Con el toDomain() de la guía, esta lectura fallaba: no se podía reconstruir un REABIERTO.
        mvc.perform(get("/api/hallazgos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REABIERTO"))
                .andExpect(jsonPath("$.fechaCierre").doesNotExist());
    }

    @Test
    void reabrirUnHallazgoQueSigueAbiertoRetorna400ConTransicionInvalida() throws Exception {
        String id = registrar("Usuarios sin MFA", "Seguridad", "ALTA");

        mvc.perform(patch("/api/hallazgos/{id}/reabrir", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Prueba\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipo").value("TransicionInvalidaException"));
    }

    @Test
    void unIdInexistenteRetorna404YUnIdMalFormadoRetorna400() throws Exception {
        mvc.perform(get("/api/hallazgos/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/hallazgos/{id}", "no-es-un-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarSinTituloRetorna400ConElDetalle() throws Exception {
        mvc.perform(post("/api/hallazgos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"","areaResponsable":"Infraestructura","severidad":"ALTA","fechaDeteccion":"2026-08-01"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", hasItem("titulo: es obligatorio")));
    }

    @Test
    void listarIncluyeLosHallazgosRegistrados() throws Exception {
        String id = registrar("Inventario desactualizado", "Activos", "MEDIA");

        mvc.perform(get("/api/hallazgos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(id)));
    }
}
