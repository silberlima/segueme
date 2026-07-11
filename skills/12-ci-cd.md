# CI/CD

## Objetivo

Toda alteração deve ser validada automaticamente.

---

# Pipeline

Pipeline mínimo:

Checkout

↓

Compile

↓

Testes Unitários

↓

Testes Integração

↓

Análise de Qualidade

↓

Build Docker

↓

Deploy

---

# Qualidade

Pipeline deve falhar quando:

Testes falharem

Cobertura mínima não for atingida

Build falhar

Lint falhar

---

# Versionamento

Utilizar Semantic Versioning.

Exemplo:

1.0.0

1.1.0

1.1.1

---

# Build

Sempre gerar:

Docker Image

Tag baseada na versão.

Nunca utilizar latest em produção.

---

# Deploy

Utilizar ambientes:

Development

Homologação

Produção

Deploy sempre automatizado.
