# Testes

## Objetivo

Garantir que toda funcionalidade implementada possua testes automatizados para evitar regressões.

---

# Ferramentas

Obrigatório utilizar:

- JUnit 5
- Mockito
- Spring Boot Test
- Testcontainers
- MockMvc

Nunca utilizar frameworks de teste descontinuados.

---

# Estrutura

Criar testes em:

src/test/java

Mantendo exatamente a mesma estrutura do código fonte.

Exemplo:

src/main/java/com/app/user/UserService.java

↓

src/test/java/com/app/user/UserServiceTest.java

---

# Testes Unitários

Todo Use Case deve possuir testes unitários.

Todo Domain Service deve possuir testes unitários.

Controllers não precisam de testes unitários.

---

# O que deve ser mockado

Mockar apenas:

- Repository
- Clients externos
- APIs externas
- Serviços de terceiros

Nunca mockar:

- Entidades
- DTOs
- Value Objects

---

# Testes de Integração

Utilizar:

@SpringBootTest

Sempre utilizar Testcontainers.

Nunca utilizar banco H2.

Banco de testes:

PostgreSQL

---

# Cobertura

Objetivo mínimo:

80%

Casos obrigatórios:

- Fluxo feliz
- Validação
- Exceções
- Permissões
- Dados inválidos

---

# Nome dos testes

Utilizar padrão:

shouldCreateUser()

shouldReturn404WhenUserNotExists()

shouldThrowExceptionWhenEmailAlreadyExists()

Nunca utilizar:

test1()

teste()

---

# AAA Pattern

Todos os testes devem seguir:

Arrange

Act

Assert

---

# Qualidade

Cada teste deve validar apenas um comportamento.

Nunca criar testes gigantes.

Preferir vários testes pequenos.

---

# Performance

Testes devem ser independentes.

Nunca depender da ordem de execução.
