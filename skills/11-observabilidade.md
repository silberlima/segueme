# Observabilidade

## Objetivo

Toda aplicação deve fornecer informações suficientes para monitoramento e diagnóstico.

---

# Ferramentas

Spring Boot Actuator

Micrometer

Prometheus

Grafana

---

# Endpoints

Obrigatórios:

/health

/info

/metrics

/prometheus

---

# Logs

Utilizar logs estruturados.

Nunca utilizar:

System.out.println()

---

# Log obrigatório

Login

Logout

Erro inesperado

Falha autenticação

Integrações

Tempo de processamento

---

# Métricas

Tempo médio de resposta

Quantidade de requisições

Quantidade de erros

Uso de memória

Uso de CPU

Pool de conexões

---

# Health Check

Implementar verificações:

Banco

Keycloak

Integrações externas
