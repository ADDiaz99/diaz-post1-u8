package com.example.auditoria.config;

import com.example.auditoria.usecase.CerrarHallazgoUseCase;
import com.example.auditoria.usecase.ConsultarHallazgoUseCase;
import com.example.auditoria.usecase.ConsultarHistorialUseCase;
import com.example.auditoria.usecase.IniciarRemediacionUseCase;
import com.example.auditoria.usecase.ReabrirHallazgoUseCase;
import com.example.auditoria.usecase.RegistrarHallazgoUseCase;
import com.example.auditoria.usecase.impl.CerrarHallazgoService;
import com.example.auditoria.usecase.impl.ConsultarHallazgoService;
import com.example.auditoria.usecase.impl.ConsultarHistorialService;
import com.example.auditoria.usecase.impl.IniciarRemediacionService;
import com.example.auditoria.usecase.impl.ReabrirHallazgoService;
import com.example.auditoria.usecase.impl.RegistrarHallazgoService;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;
import com.example.auditoria.usecase.port.HistorialAuditoriaPort;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * Wiring explícito (círculo Frameworks & Drivers).
 *
 * Ajuste sobre la guía: la bitácora debe escribirse en la MISMA transacción que
 * el cambio de estado, pero los casos de uso no pueden llevar @Transactional sin
 * importar Spring. Sin nada más, repo.guardar() e historial.registrar() serían dos
 * transacciones: si la segunda falla, el hallazgo cambia de estado sin dejar
 * rastro. La solución es envolver cada caso de uso en un proxy transaccional
 * aquí, en el círculo externo: el caso de uso sigue siendo Java puro y la
 * transacción es un detalle de infraestructura.
 */
@Configuration
public class AuditoriaConfiguration {

    private final PlatformTransactionManager transactionManager;

    public AuditoriaConfiguration(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Bean
    public RegistrarHallazgoUseCase registrarHallazgoUseCase(HallazgoRepositoryPort repo,
                                                             HistorialAuditoriaPort historial) {
        return transaccional(RegistrarHallazgoUseCase.class, new RegistrarHallazgoService(repo, historial));
    }

    @Bean
    public IniciarRemediacionUseCase iniciarRemediacionUseCase(HallazgoRepositoryPort repo,
                                                               HistorialAuditoriaPort historial) {
        return transaccional(IniciarRemediacionUseCase.class, new IniciarRemediacionService(repo, historial));
    }

    @Bean
    public CerrarHallazgoUseCase cerrarHallazgoUseCase(HallazgoRepositoryPort repo,
                                                       HistorialAuditoriaPort historial) {
        return transaccional(CerrarHallazgoUseCase.class, new CerrarHallazgoService(repo, historial));
    }

    @Bean
    public ReabrirHallazgoUseCase reabrirHallazgoUseCase(HallazgoRepositoryPort repo,
                                                         HistorialAuditoriaPort historial) {
        return transaccional(ReabrirHallazgoUseCase.class, new ReabrirHallazgoService(repo, historial));
    }

    @Bean
    public ConsultarHallazgoUseCase consultarHallazgoUseCase(HallazgoRepositoryPort repo) {
        return new ConsultarHallazgoService(repo);
    }

    @Bean
    public ConsultarHistorialUseCase consultarHistorialUseCase(HallazgoRepositoryPort repo,
                                                               HistorialAuditoriaPort historial) {
        return new ConsultarHistorialService(repo, historial);
    }

    /**
     * Proxy que abre una transacción al entrar al caso de uso y hace commit al salir,
     * o rollback si sale con una excepción no verificada (RuntimeException).
     */
    private <T> T transaccional(Class<T> contrato, T casoDeUso) {
        ProxyFactory proxy = new ProxyFactory(casoDeUso);
        proxy.addInterface(contrato);
        proxy.addAdvice(new TransactionInterceptor(transactionManager, new MatchAlwaysTransactionAttributeSource()));
        return contrato.cast(proxy.getProxy());
    }
}
