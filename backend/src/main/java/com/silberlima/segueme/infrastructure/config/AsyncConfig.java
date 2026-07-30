package com.silberlima.segueme.infrastructure.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Pool dedicado à geração de relatórios (Fase 13). É limitado de propósito: uma varredura de
 * escopo nacional é cara, e sem teto um pico de solicitações competiria com as requisições comuns
 * pelo pool de conexões do banco. Solicitações além do limite esperam na fila em vez de serem
 * recusadas — o usuário já recebeu o "pendente" e acompanha pela tela.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("relatoriosExecutor")
    public Executor relatoriosExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("relatorios-");
        executor.initialize();
        return executor;
    }
}
