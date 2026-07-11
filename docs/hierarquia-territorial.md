# Hierarquia Territorial — Dioceses e Paróquias

## 1. Objetivo
Apresentar e gerenciar a estrutura eclesiástica-territorial (Nacional → Diocese → Setor → Paróquia) usada como base da hierarquia de acesso e escopo de dados.

## 2. Telas envolvidas

### 2.1 Dioceses (`screen_dioceses`)
- **Objetivo:** visão hierárquica completa Nacional → Diocese → Setor → Paróquia.
- **Quem acessa:** perfil Diocese (e Nacional, por herança de escopo).
- **Componentes:** tabela hierárquica mostrando os níveis Nacional, Diocese, Setor, Paróquia, com métricas agregadas (paróquias, seguidores) por nível.

### 2.2 Paróquias (`screen_paroquias`)
- **Objetivo:** listar as paróquias dentro do escopo do usuário (setor, no caso do perfil Setorial; diocese, no caso do perfil Diocese).
- **Componentes:** cards de paróquia com estatísticas (seguidores, status do encontro atual — ex.: "62º em 14d", "Ativo", ou "—" quando sem encontro corrente).

## 3. Regras de negócio
- **RN-HIERARQUIA-01:** a estrutura territorial é estritamente hierárquica: Nacional → Diocese/Arquidiocese → Setor → Paróquia.
- **RN-HIERARQUIA-02:** cada perfil de acesso territorial (Nacional, Diocese, Setorial, Paróquia) só visualiza dados do seu nível e dos níveis abaixo dele.
- **RN-HIERARQUIA-03:** uma paróquia pertence a exatamente um setor; um setor pertence a exatamente uma diocese.

## 4. Casos de uso
- **UC — Consultar estrutura hierárquica:** Ator: Diocese ou Nacional. Fluxo principal: abrir Dioceses → visualizar tabela Nacional/Diocese/Setor/Paróquia.
- **UC — Consultar paróquias do escopo:** Ator: Setorial ou Diocese. Fluxo principal: abrir Paróquias → visualizar cards com estatísticas.

## 5. Regras pendentes de validação
- Fluxo de cadastro/edição de uma nova Diocese, Setor ou Paróquia não está presente no protótipo — apenas telas de consulta foram encontradas. Presume-se que esse cadastro seja administrado em nível Nacional, mas isso precisa confirmação.
- Regra de o que ocorre com pessoas e encontros de uma paróquia caso ela seja desativada/removida não especificada.
