# Qualidade

## Princípios

Seguir:

- SOLID
- Clean Code

## Métodos

Métodos pequenos.

Evitar mais de 30 linhas quando possível.

## Classes

Uma responsabilidade.

## Tratamento de exceções

Utilizar:

GlobalExceptionHandler

Criar exceções específicas.

Nunca retornar stack trace.

## Validação

Utilizar Jakarta Validation.

Exemplo:

@NotNull

@NotBlank

@Email

@Size

Nunca validar manualmente no Controller.

## Testes

Criar:

- testes unitários
- testes de integração

## Mapeamento

Utilizar MapStruct.

Não realizar conversões diretamente no Controller.