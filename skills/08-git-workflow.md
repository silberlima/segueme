# Git Workflow

## Branches

Utilizar:

main

develop

feature/*

bugfix/*

hotfix/*

release/*

---

# Nome das branches

feature/user-registration

feature/create-event

bugfix/login

Nunca utilizar:

teste

nova

feature1

---

# Conventional Commits

Obrigatório.

Tipos:

feat:

fix:

refactor:

docs:

style:

test:

perf:

ci:

build:

chore:

---

# Exemplos

feat: create user registration

fix: validate duplicated email

refactor: simplify authentication service

docs: update API documentation

---

# Pull Requests

Todo Pull Request deve conter:

Objetivo

Descrição

Impactos

Checklist

Screenshots (Frontend)

---

# Revisão

Nunca aprovar código contendo:

TODO

FIXME

Código comentado

Duplicação

Métodos gigantes

---

# Merge

Preferir:

Squash Merge

Manter histórico limpo.

Nunca realizar commits diretamente na main.
