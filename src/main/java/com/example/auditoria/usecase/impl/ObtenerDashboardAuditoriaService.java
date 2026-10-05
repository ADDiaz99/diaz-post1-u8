package com.example.auditoria.usecase.impl;

import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.Severidad;
import com.example.auditoria.usecase.ObtenerDashboardAuditoriaUseCase;
import com.example.auditoria.usecase.port.ConteoCategoria;
import com.example.auditoria.usecase.port.DashboardAuditoriaView;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;
import com.example.auditoria.usecase.port.PromedioCategoria;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ObtenerDashboardAuditoriaService implements ObtenerDashboardAuditoriaUseCase {

    private final HallazgoRepositoryPort repo;

    public ObtenerDashboardAuditoriaService(HallazgoRepositoryPort repo) {
        this.repo = repo;
    }

    /**
     * GROUP BY solo devuelve las categorías que tienen hallazgos. El comité necesita
     * ver también los ceros ("0 críticos" es un dato), así que se completan las
     * cuatro severidades y los cuatro estados, en el orden en que están definidos.
     */
    @Override
    public DashboardAuditoriaView ejecutar() {
        List<PromedioCategoria> promedios = repo.promedioDiasCierrePorArea().stream()
                .sorted(Comparator.comparing(PromedioCategoria::categoria))
                .toList();
        return new DashboardAuditoriaView(
                completar(repo.contarPorSeveridad(), Arrays.stream(Severidad.values()).map(Enum::name).toList()),
                completar(repo.contarPorEstado(), Arrays.stream(EstadoHallazgo.values()).map(Enum::name).toList()),
                promedios);
    }

    private static List<ConteoCategoria> completar(List<ConteoCategoria> conteos, List<String> categorias) {
        Map<String, Long> porCategoria = conteos.stream()
                .collect(Collectors.toMap(ConteoCategoria::categoria, ConteoCategoria::total));
        return categorias.stream()
                .map(categoria -> new ConteoCategoria(categoria, porCategoria.getOrDefault(categoria, 0L)))
                .toList();
    }
}
