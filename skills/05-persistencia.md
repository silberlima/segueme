# Persistência

## Banco

PostgreSQL

## Migrações

Utilizar Liquibase.

Toda alteração estrutural deve gerar nova migration.

Nunca alterar migrations já executadas.

## Repository

Utilizar Spring Data JPA.

Não acessar banco diretamente pelo Controller.

## Entidades

Todas as entidades devem possuir:

id

createdAt

updatedAt

createdBy

updatedBy

deleted

## Auditoria

Utilizar auditoria automática.

## Soft Delete

Preferencialmente utilizar soft delete.

## Performance

Evitar consultas N+1.

Utilizar Fetch Join quando necessário.

Não carregar relacionamentos desnecessários.

## Transações

Utilizar @Transactional apenas onde necessário.