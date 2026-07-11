# Segurança

## Autenticação

Utilizar:

- Spring Security
- OAuth2
- JWT
- Keycloak

## Login Social

Permitir autenticação via:

- Google
- Instagram

## Autorização

Utilizar Roles.

Exemplo:

ROLE_ADMIN

ROLE_USER

Nunca verificar permissões diretamente nos Controllers.

## Senhas

Nunca armazenar senhas em texto.

Sempre utilizar BCrypt.

## Tokens

JWT deve conter:

- id
- email
- roles