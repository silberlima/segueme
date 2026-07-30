# Observabilidade

Dashboards e alertas sobre as métricas que o Actuator + Micrometer já expõem em
`/actuator/prometheus` desde a Fase 0. Nada aqui exige código novo na aplicação — só a
configuração de `management.metrics` em `application.yml`, que liga o histograma de latência (sem
ele não existe p95) e a tag `application`.

## Subir

```bash
docker compose -f docker-compose.yml -f docker-compose.observabilidade.yml up -d
```

- Grafana: <http://localhost:3000> (`admin`/`admin`) — dashboard **Segue-me · Backend** já
  provisionado, datasource incluso.
- Prometheus: <http://localhost:9090> — alertas em **Alerts**.

## O que está no dashboard

| Painel | Para quê |
| --- | --- |
| Disponibilidade, req/s, erro 5xx, 403/min, conexões na fila | leitura de 5 segundos do estado do serviço |
| Latência p95 por endpoint | RNF-09/10 — onde a tela demora |
| Requisições por status / endpoints mais chamados | onde o tráfego está e o que quebra |
| JVM e pool Hikari | saturação antes de virar erro |

## Alertas (`alertas.yml`)

| Alerta | Dispara quando | Severidade |
| --- | --- | --- |
| `BackendForaDoAr` | scrape falhando por 2 min | crítico |
| `HealthDegradado` | `/actuator/health` sai de UP por 2 min | crítico |
| `TaxaDeErro5xxAlta` | >5% de 5xx por 5 min | crítico |
| `PicoDe403` | >1 req/s de 403 por 10 min — sinal de claim territorial errada no Keycloak (RNF-06) | atenção |
| `LatenciaP95Alta` | p95 de um endpoint acima de 2s por 10 min | atenção |
| `PoolDeConexoesEsgotado` | requisições na fila do Hikari por 5 min | atenção |
| `RelatoriosFalhando` | repositórios lançando exceção — a geração de relatório roda fora da requisição e falharia em silêncio | atenção |

**Roteamento de notificação não está configurado**: as regras avaliam e ficam visíveis no
Prometheus, mas não há Alertmanager porque o canal (e-mail, Slack, plantão) ainda não foi definido
com o PO. Definido o canal, é acrescentar o serviço `alertmanager` a este mesmo override e o bloco
`alerting` ao `prometheus.yml` — as regras não mudam.
