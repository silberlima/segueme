# Convenções

## Nome de classes

Controller

UserController

Service

UserService

Repository

UserRepository

Entity

UserEntity

Mapper

UserMapper

UseCase

CreateUserUseCase

UpdateUserUseCase

DeleteUserUseCase

## Nome de métodos

Utilizar verbos claros.

Exemplo:

create()

update()

delete()

findById()

findAll()

## Organização

Cada funcionalidade deve possuir sua própria estrutura.

Exemplo:

user/

controller/

dto/

entity/

repository/

mapper/

usecase/

validator/

exception/

## Código

Nunca comentar código desnecessariamente.

O código deve ser autoexplicativo.

Evitar duplicação.

Sempre preferir composição à herança.

Nunca deixar código morto.

Nunca criar métodos gigantes.

Nunca criar classes gigantes.