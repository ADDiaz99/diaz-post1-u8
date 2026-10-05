package com.example.auditoria.adapter.in.web;

import com.example.auditoria.adapter.in.web.dto.HallazgoResponse;
import com.example.auditoria.adapter.in.web.dto.IniciarRemediacionRequest;
import com.example.auditoria.adapter.in.web.dto.ReabrirRequest;
import com.example.auditoria.adapter.in.web.dto.RegistrarHallazgoRequest;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.usecase.CerrarHallazgoUseCase;
import com.example.auditoria.usecase.ConsultarHallazgoUseCase;
import com.example.auditoria.usecase.ConsultarHistorialUseCase;
import com.example.auditoria.usecase.IniciarRemediacionUseCase;
import com.example.auditoria.usecase.ObtenerDashboardAuditoriaUseCase;
import com.example.auditoria.usecase.ReabrirHallazgoUseCase;
import com.example.auditoria.usecase.RegistrarHallazgoUseCase;
import com.example.auditoria.usecase.port.CambioEstadoView;
import com.example.auditoria.usecase.port.DashboardAuditoriaView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Interface Adapter de entrada: traduce HTTP a llamadas a los casos de uso.
 *
 * El usuario que origina cada cambio llega en la cabecera X-Usuario. Este
 * laboratorio no tiene autenticación, así que es un dato declarado; en
 * producción vendría del usuario autenticado (por ejemplo, Spring Security).
 */
@RestController
@RequestMapping("/api/hallazgos")
public class HallazgoController {

    private final RegistrarHallazgoUseCase registrarUseCase;
    private final IniciarRemediacionUseCase iniciarRemediacionUseCase;
    private final CerrarHallazgoUseCase cerrarUseCase;
    private final ReabrirHallazgoUseCase reabrirUseCase;
    private final ConsultarHallazgoUseCase consultarUseCase;
    private final ObtenerDashboardAuditoriaUseCase dashboardUseCase;
    private final ConsultarHistorialUseCase consultarHistorialUseCase;

    static final String CABECERA_USUARIO = "X-Usuario";

    public HallazgoController(RegistrarHallazgoUseCase registrarUseCase,
                              IniciarRemediacionUseCase iniciarRemediacionUseCase,
                              CerrarHallazgoUseCase cerrarUseCase,
                              ReabrirHallazgoUseCase reabrirUseCase,
                              ConsultarHallazgoUseCase consultarUseCase,
                              ObtenerDashboardAuditoriaUseCase dashboardUseCase,
                              ConsultarHistorialUseCase consultarHistorialUseCase) {
        this.registrarUseCase = registrarUseCase;
        this.iniciarRemediacionUseCase = iniciarRemediacionUseCase;
        this.cerrarUseCase = cerrarUseCase;
        this.reabrirUseCase = reabrirUseCase;
        this.consultarUseCase = consultarUseCase;
        this.dashboardUseCase = dashboardUseCase;
        this.consultarHistorialUseCase = consultarHistorialUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> registrar(@Valid @RequestBody RegistrarHallazgoRequest req,
                                         @RequestHeader(name = CABECERA_USUARIO, defaultValue = "anonimo") String usuario) {
        HallazgoId id = registrarUseCase.ejecutar(
                req.titulo(), req.descripcion(), req.areaResponsable(), req.severidad(), req.fechaDeteccion(), usuario);
        return Map.of("hallazgoId", id.toString());
    }

    @PatchMapping("/{id}/iniciar-remediacion")
    public Map<String, String> iniciarRemediacion(@PathVariable String id,
                                                  @Valid @RequestBody IniciarRemediacionRequest req,
                                                  @RequestHeader(name = CABECERA_USUARIO, defaultValue = "anonimo") String usuario) {
        iniciarRemediacionUseCase.ejecutar(aHallazgoId(id), req.responsable(), req.fechaLimite(), req.notas(), usuario);
        return Map.of("estado", "EN_REMEDIACION");
    }

    @PatchMapping("/{id}/cerrar")
    public Map<String, String> cerrar(@PathVariable String id,
                                      @RequestHeader(name = CABECERA_USUARIO, defaultValue = "anonimo") String usuario) {
        cerrarUseCase.ejecutar(aHallazgoId(id), usuario);
        return Map.of("estado", "CERRADO");
    }

    @PatchMapping("/{id}/reabrir")
    public Map<String, String> reabrir(@PathVariable String id, @Valid @RequestBody ReabrirRequest req,
                                       @RequestHeader(name = CABECERA_USUARIO, defaultValue = "anonimo") String usuario) {
        reabrirUseCase.ejecutar(aHallazgoId(id), req.motivo(), usuario);
        return Map.of("estado", "REABIERTO");
    }

    @GetMapping("/{id}")
    public HallazgoResponse buscar(@PathVariable String id) {
        return HallazgoResponse.desde(consultarUseCase.buscarPorId(aHallazgoId(id)));
    }

    @GetMapping
    public List<HallazgoResponse> listar() {
        return consultarUseCase.listarTodos().stream().map(HallazgoResponse::desde).toList();
    }

    /** Ruta literal: Spring la prefiere sobre /{id}, así "dashboard" nunca se interpreta como un id. */
    @GetMapping("/dashboard")
    public DashboardAuditoriaView dashboard() {
        return dashboardUseCase.ejecutar();
    }

    @GetMapping("/{id}/historial")
    public List<CambioEstadoView> historial(@PathVariable String id) {
        return consultarHistorialUseCase.ejecutar(aHallazgoId(id));
    }

    /** UUID.fromString lanza IllegalArgumentException si el id no es un UUID: se responde 400. */
    private static HallazgoId aHallazgoId(String id) {
        return new HallazgoId(UUID.fromString(id));
    }
}
