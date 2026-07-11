# Equipe Dirigente e Equipe Comando

## 1. Objetivo
Documentar as duas estruturas de liderança de um Encontro: a Equipe Dirigente (formada antes do encontro existir, na criação) e a Equipe Comando (que executa o encontro, convidando as demais equipes).

## 2. Telas envolvidas

### 2.1 Equipe Dirigente (`screen_dirigente`)
- **Objetivo:** navegar e consultar a composição das 5 pastas da Equipe Dirigente de um encontro.
- **Quem acessa:** perfis com visão de encontro (Paróquia e superiores).
- **Descrição funcional:** 5 pastas navegáveis (Coordenação Geral, Formação, Liturgia, Secretaria, Logística — nomenclatura observada nesta tela, distinta da nomenclatura usada no wizard de criação "Finanças/Fichas/Palestras/Pós-Encontro/Montagem"; **ver pendência de nomenclatura abaixo**), com estado de pasta selecionada controlado por navegação interna.
- **Ações:** alternar entre as 5 pastas.

### 2.2 Equipe Comando (`screen_comando_eq`)
- **Objetivo:** ver o roster e status da Equipe Comando de um encontro.
- **Conteúdo:** cabeçalho do encontro + roster de integrantes da Comando com status (aceito/pendente/recusado).
- **Regra de negócio:** a Equipe Comando é responsável por convidar os coordenadores das demais equipes e acompanhar aceites (ver `convites-aceites-recusas.md`).

## 3. Regras de negócio
- **RN-DIRIGENTE-01:** a Equipe Dirigente é composta por 5 pastas fixas, cada uma com 2 jovens e 1 casal, acompanhadas pelo diretor espiritual (ver também `gerenciamento-de-encontros.md`).
- **RN-COMANDO-01:** a Equipe Comando recebe o encontro pronto da Equipe Dirigente e é responsável pela etapa de execução: convite de coordenadores e acompanhamento de aceites/recusas.

## 4. Regras pendentes de validação
- **Divergência de nomenclatura das pastas:** o wizard de criação de encontro lista as pastas da Equipe Dirigente como "Finanças, Fichas, Palestras, Pós-Encontro, Montagem"; a tela `screen_dirigente` (consulta) lista "Coordenação Geral, Formação, Liturgia, Secretaria, Logística". **Isto precisa ser validado com o responsável pelo produto** — pode ser um mesmo conceito com nomes diferentes por etapa (planejamento vs. execução) ou uma inconsistência do protótipo a ser resolvida antes da implementação.
- Regras de permissão sobre quem pode editar a composição da Equipe Dirigente e da Equipe Comando após a criação do encontro não especificadas.
