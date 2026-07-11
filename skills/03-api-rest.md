# API REST

## Convenção

Sempre utilizar:

GET

POST

PUT

PATCH

DELETE

## Endpoints

Exemplo:

/api/v1/users

/api/v1/users/{id}

## DTO

Nunca expor entidades JPA.

Sempre utilizar:

- Request DTO
- Response DTO

## Versionamento

Todas as APIs devem iniciar com:

/api/v1

## Paginação

Toda listagem deve utilizar:

page

size

sort

## Status HTTP

200 OK

201 Created

204 No Content

400 Bad Request

401 Unauthorized

403 Forbidden

404 Not Found

409 Conflict

500 Internal Server Error

## Swagger

Todos os endpoints devem ser documentados utilizando OpenAPI.