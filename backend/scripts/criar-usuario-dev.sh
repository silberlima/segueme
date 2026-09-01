#!/usr/bin/env bash
#
# Cria (ou atualiza) no Keycloak um usuário de desenvolvimento com os 10 perfis do sistema
# (RN-ACESSO-02), para exercitar qualquer tela sem precisar de um login por perfil.
#
# O realm importado pelo compose (`segueme-realm.json`) traz as 10 roles mas nenhum usuário —
# criar usuário é responsabilidade de quem sobe o ambiente, e não do realm compartilhado. Este
# script faz isso pela API de admin do Keycloak, e é idempotente: rodar de novo apenas
# reatribui roles, atributos e senha ao usuário que já existe.
#
# Uso (com a infraestrutura no ar):
#   ./backend/scripts/criar-usuario-dev.sh
#
# Variáveis de ambiente aceitas (todas com padrão de desenvolvimento):
#   KEYCLOAK_URL, KEYCLOAK_ADMIN, KEYCLOAK_ADMIN_PASSWORD,
#   DEV_EMAIL, DEV_SENHA, POSTGRES_CONTAINER, DB_NAME, DB_USERNAME
set -euo pipefail

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8081}"
KEYCLOAK_ADMIN="${KEYCLOAK_ADMIN:-admin}"
KEYCLOAK_ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-admin}"
REALM="segueme"

DEV_EMAIL="${DEV_EMAIL:-dev@segueme.test}"
DEV_SENHA="${DEV_SENHA:-dev}"

# Território do seed de demonstração (hierarquia-0002 / pessoas-0003): Diocese Central >
# Setor 1 > Paróquia São José. Com NACIONAL entre os perfis, o EscopoTerritorialResolver
# resolve escopo nacional e ignora as claims; elas ficam aqui para o caso de alguém remover
# NACIONAL do usuário para testar um escopo mais estreito.
DIOCESE_ID="00000000-0000-4000-8000-000000000001"
SETOR_ID="00000000-0000-4000-8000-000000000011"
PAROQUIA_ID="00000000-0000-4000-8000-000000000111"

# Pessoa do seed que o usuário "é" — dá conteúdo a Meus Dados e Meu Histórico (perfil Seguidor),
# que respondem 404 quando o `sub` do token não está vinculado a nenhuma Pessoa.
PESSOA_ID="00000000-0000-4000-8000-000000000301"

POSTGRES_CONTAINER="${POSTGRES_CONTAINER:-backend-postgres-1}"
DB_NAME="${DB_NAME:-segueme}"
DB_USERNAME="${DB_USERNAME:-segueme}"

PERFIS=(NACIONAL DIOCESE SETORIAL PAROQUIA EQUIPE_FICHAS EQUIPE_MONTAGEM EQUIPE_COMANDO EQUIPE_FINANCAS POS_ENCONTRO SEGUIDOR)

command -v jq >/dev/null || { echo "Erro: este script precisa do jq (brew install jq)." >&2; exit 1; }

# --- Token de admin -------------------------------------------------------------------------

TOKEN=$(curl -sS --fail-with-body \
  -d "grant_type=password" -d "client_id=admin-cli" \
  -d "username=${KEYCLOAK_ADMIN}" -d "password=${KEYCLOAK_ADMIN_PASSWORD}" \
  "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
  | jq -r .access_token) || {
    echo "Erro: não foi possível autenticar como admin em ${KEYCLOAK_URL}." >&2
    echo "Suba a infraestrutura com: docker compose -f backend/docker-compose.yml up -d postgres keycloak" >&2
    exit 1
  }

# Chama a API de admin do realm. Uso: admin <método> <caminho> [corpo-json]
admin() {
  local metodo="$1" caminho="$2" corpo="${3:-}"
  if [[ -n "${corpo}" ]]; then
    curl -sS --fail-with-body -X "${metodo}" \
      -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" \
      -d "${corpo}" "${KEYCLOAK_URL}/admin/realms/${REALM}/${caminho}"
  else
    curl -sS --fail-with-body -X "${metodo}" \
      -H "Authorization: Bearer ${TOKEN}" "${KEYCLOAK_URL}/admin/realms/${REALM}/${caminho}"
  fi
}

# --- Usuário --------------------------------------------------------------------------------

USUARIO=$(jq -n \
  --arg email "${DEV_EMAIL}" \
  --arg diocese "${DIOCESE_ID}" --arg setor "${SETOR_ID}" --arg paroquia "${PAROQUIA_ID}" \
  '{
     username: $email, email: $email,
     firstName: "Usuário", lastName: "Dev",
     enabled: true, emailVerified: true,
     attributes: { diocese_id: [$diocese], setor_id: [$setor], paroquia_id: [$paroquia] }
   }')

# O id do usuário é gerado pelo Keycloak (a versão 26 ignora um `id` enviado na criação), então
# ele é sempre resolvido pelo username — é esse valor que chega ao backend como `sub` do token e
# que a coluna `pessoa.usuario_id` guarda.
buscar_id() {
  admin GET "users?username=$(printf '%s' "${DEV_EMAIL}" | jq -sRr @uri)&exact=true" | jq -r '.[0].id // empty'
}

DEV_ID=$(buscar_id)
if [[ -n "${DEV_ID}" ]]; then
  admin PUT "users/${DEV_ID}" "${USUARIO}" >/dev/null
  echo "Usuário ${DEV_EMAIL} atualizado."
else
  admin POST "users" "${USUARIO}" >/dev/null
  DEV_ID=$(buscar_id)
  [[ -n "${DEV_ID}" ]] || { echo "Erro: usuário não encontrado logo após ser criado." >&2; exit 1; }
  echo "Usuário ${DEV_EMAIL} criado."
fi

admin PUT "users/${DEV_ID}/reset-password" \
  "$(jq -n --arg senha "${DEV_SENHA}" '{type: "password", value: $senha, temporary: false}')" >/dev/null

# --- Perfis ---------------------------------------------------------------------------------

ROLES="[]"
for perfil in "${PERFIS[@]}"; do
  role=$(admin GET "roles/${perfil}")
  ROLES=$(jq -n --argjson acc "${ROLES}" --argjson role "${role}" '$acc + [$role]')
done
admin POST "users/${DEV_ID}/role-mappings/realm" "${ROLES}" >/dev/null
echo "Perfis atribuídos: ${PERFIS[*]}"

# --- Vínculo com a Pessoa do seed -----------------------------------------------------------

# O vínculo usuário↔Pessoa vive no banco, não no Keycloak, e só existe para o ambiente de
# desenvolvimento — por isso fica aqui e não numa migration, que rodaria também em produção.
# Falhar aqui não invalida o que já foi feito no Keycloak: o login funciona de qualquer jeito.
if docker exec -i "${POSTGRES_CONTAINER}" psql -v ON_ERROR_STOP=1 -q -U "${DB_USERNAME}" -d "${DB_NAME}" <<SQL 2>/dev/null
begin;
update pessoa set usuario_id = null where usuario_id = '${DEV_ID}' and id <> '${PESSOA_ID}';
update pessoa set usuario_id = '${DEV_ID}', updated_at = now() where id = '${PESSOA_ID}';
commit;
SQL
then
  echo "Vinculado à Pessoa ${PESSOA_ID} (Ana Beatriz Souza) — Meus Dados e Meu Histórico populados."
else
  echo "Aviso: não foi possível vincular a Pessoa no PostgreSQL (container \"${POSTGRES_CONTAINER}\")." >&2
  echo "       O login funciona, mas Meus Dados e Meu Histórico responderão 404 até o vínculo existir." >&2
fi

echo
echo "Pronto. Entre em http://localhost:5173/login com:"
echo "  E-mail: ${DEV_EMAIL}"
echo "  Senha:  ${DEV_SENHA}"
echo "Troque de perfil pelo seletor no topo da sidebar."
