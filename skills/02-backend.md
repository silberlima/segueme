# Backend

## Stack

Obrigatório utilizar:

- Java 21+
- Spring Boot 4+
- Spring Data JPA
- PostgreSQL
- Liquibase
- Docker
- Maven

## Lombok

Não utilizar Lombok.

Todos os getters, setters e construtores devem ser explícitos.

## Configuração

Utilizar:

application.yml

Perfis:

- dev
- test
- prod

Nunca armazenar senhas no código.

Sempre utilizar variáveis de ambiente.

## Docker

Todo projeto deve possuir:

- Dockerfile
- docker-compose.yml

docker-compose deve subir:

- aplicação
- PostgreSQL
- Keycloak

## Logs

Utilizar SLF4J.

Nunca utilizar:

System.out.println()

Utilizar níveis:

- INFO
- WARN
- ERROR
- DEBUG