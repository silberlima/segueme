package com.silberlima.segueme.support;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Container "singleton" (padrão documentado do Testcontainers): iniciado uma única vez em bloco
 * estático e nunca parado entre classes de teste. Sem isso, o ciclo de vida por-classe de
 * {@code @Testcontainers}/{@code @Container} reinicia o container (nova porta) entre classes de
 * IT consecutivas, e o cache de contexto do Spring pode reaproveitar um {@code DataSource} já
 * apontando para a porta antiga — causando falhas intermitentes de conexão recusada.
 */
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }
}
