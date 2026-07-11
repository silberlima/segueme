# Arquitetura

## Objetivo

Todas as implementações devem seguir Domain Driven Design (DDD) com separação clara das responsabilidades.

## Arquitetura

Utilizar arquitetura em camadas:

- API
- Application
- Domain
- Infrastructure

Nunca misturar responsabilidades entre camadas.

## Responsabilidades

### API

- Controllers
- Requests
- Responses
- Validação de entrada

Não conter regra de negócio.

### Application

- Use Cases
- Orquestração
- DTOs

Responsável por executar os casos de uso.

### Domain

Contém:

- Entidades
- Value Objects
- Interfaces de Repositórios
- Domain Services
- Exceptions

Toda regra de negócio deve estar nesta camada.

### Infrastructure

Contém:

- Implementação dos repositórios
- Configurações
- Segurança
- Banco de dados
- Integrações externas

## Dependências

As dependências devem sempre apontar para dentro.

Infrastructure → Domain

API → Application

Application → Domain

Nunca permitir dependência inversa.

## Boas práticas

Uma classe deve possuir apenas uma responsabilidade.

Use Cases devem representar uma única ação do sistema.

Nunca criar classes genéricas como:

- Utils
- Helper
- Common
- Manager

Sempre utilizar nomes que representem o domínio.