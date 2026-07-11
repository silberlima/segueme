# Casos de Uso Consolidados

Casos de uso já detalhados dentro de cada arquivo funcional são referenciados aqui por nome; esta lista serve como índice único com atores.

## UC-01 — Autenticar no sistema
**Ator:** qualquer usuário do movimento com credencial cadastrada.
**Pré-condições:** possuir e-mail e senha cadastrados.
**Fluxo principal:** informar e-mail e senha → confirmar → acessar o Mapa de Navegação.
**Fluxos alternativos:** "Esqueci a senha" → UC-02.
**Pós-condições:** sessão iniciada.
**Regras relacionadas:** RN-05.
**Detalhe completo:** `autenticacao-acesso.md`.

## UC-02 — Recuperar senha
**Ator:** usuário sem acesso à conta.
**Fluxo principal:** informar e-mail cadastrado → solicitar link de recuperação.
**Detalhe completo:** `autenticacao-acesso.md`.

## UC-03 — Cadastrar pessoa
**Ator:** equipe com permissão de cadastro (paróquia/perfis afins).
**Pré-condições:** dados básicos disponíveis.
**Fluxo principal:** Lista de Pessoas → Nova pessoa → preencher formulário → salvar.
**Pós-condições:** pessoa criada com situação "Pré-inscrito".
**Regras relacionadas:** RN-01, RN-02.
**Detalhe completo:** `cadastro-de-pessoas.md`.

## UC-04 — Consultar pessoa e histórico
**Ator:** qualquer perfil com acesso a Pessoas.
**Fluxo principal:** buscar/filtrar → abrir Perfil Completo → abrir Histórico.
**Detalhe completo:** `cadastro-de-pessoas.md`.

## UC-05 — Criar encontro
**Ator:** perfil Paróquia.
**Pré-condições:** dados de identidade, local, Equipe Dirigente e Equipe Comando disponíveis.
**Fluxo principal:** wizard de 5 passos → revisão → confirmação.
**Pós-condições:** encontro criado, entregue à Equipe Comando.
**Regras relacionadas:** RN-16, RN-20, RN-21.
**Detalhe completo:** `gerenciamento-de-encontros.md`.

## UC-06 — Preencher ficha de inscrição
**Ator:** Equipe Fichas.
**Pré-condições:** candidato em "Aguardando ficha".
**Fluxo principal:** Pré-inscrições → Preencher ficha → completar 4 seções → Confirmar inscrição.
**Pós-condições:** candidato confirmado como Seguimista.
**Regras relacionadas:** RN-14, RN-15.
**Detalhe completo:** `inscricoes.md`.

## UC-07 — Promover candidato da reserva
**Ator:** Equipe Fichas.
**Pré-condições:** vaga disponível.
**Fluxo principal:** Cadastro de Reserva → Promover à vaga.
**Regras relacionadas:** RN-16.
**Detalhe completo:** `inscricoes.md`.

## UC-08 — Compor equipe via sugestão inteligente
**Ator:** Equipe Montagem.
**Pré-condições:** equipe com vaga em aberto.
**Fluxo principal:** abrir equipe → abrir sugestões → Adicionar à equipe.
**Regras relacionadas:** RN-07, RN-08, RN-09, RN-10.
**Detalhe completo:** `montagem-de-equipes.md`.

## UC-09 — Registrar substituição
**Ator:** Equipe Montagem.
**Fluxo principal:** Nova substituição → definir quem sai/entra → justificativa → registrar.
**Regras relacionadas:** RN-12, RN-13.
**Detalhe completo:** `montagem-de-equipes.md`.

## UC-10 — Convidar coordenador de equipe
**Ator:** Equipe Comando.
**Pré-condições:** equipe sem coordenador.
**Fluxo principal:** emitir convite → aguardar resposta.
**Fluxo alternativo:** recusa com justificativa → notifica Montagem.
**Regras relacionadas:** RN-11, RN-12.
**Detalhe completo:** `convites-aceites-recusas.md`.

## UC-11 — Convidar integrante de equipe
**Ator:** Coordenador de equipe (após aceitar seu próprio convite).
**Fluxo principal:** convidar pessoa indicada → aguardar resposta.
**Regras relacionadas:** RN-11, RN-12.
**Detalhe completo:** `convites-aceites-recusas.md`.

## UC-12 — Tratar recusa de convite
**Ator:** Equipe Montagem (reativo).
**Fluxo principal:** recebe notificação → abre Substituições → registra novo candidato.
**Regras relacionadas:** RN-12, RN-13.
**Detalhe completo:** `montagem-de-equipes.md`.

## UC-13 — Aprovar/recusar transferência
**Ator:** Setorial.
**Fluxo principal:** Solicitações → avaliar card de transferência → Aprovar ou Recusar.
**Regras relacionadas:** RN-17.
**Detalhe completo:** `transferencias-e-emprestimos.md`.

## UC-14 — Solicitar empréstimo de seguidor
**Ator:** Equipe Montagem.
**Fluxo principal:** Empréstimos → Nova solicitação → informar equipe/habilidade.
**Regras relacionadas:** RN-17, RN-18.
**Detalhe completo:** `transferencias-e-emprestimos.md`.

## UC-15 — Selecionar e enviar candidatos de empréstimo
**Ator:** Setorial.
**Fluxo principal:** ver solicitações do setor → filtrar por paróquia/habilidade → selecionar candidatos → Enviar à Montagem.
**Regras relacionadas:** RN-18.
**Detalhe completo:** `transferencias-e-emprestimos.md`.

## UC-16 — Efetivar empréstimo
**Ator:** Equipe Montagem.
**Pré-condições:** sugestões recebidas do Setorial.
**Fluxo principal:** revisar sugestões → Adicionar às equipes.
**Regras relacionadas:** RN-18.
**Detalhe completo:** `transferencias-e-emprestimos.md`.

## UC-17 — Registrar lançamento financeiro
**Ator:** Equipe Finanças.
**Fluxo principal:** Financeiro → Lançamento → preencher dados → salvar.
**Regras relacionadas:** RN-24.
**Detalhe completo:** `financeiro.md`.

## UC-18 — Designar palestrante
**Ator:** responsável pelo conteúdo do encontro (a confirmar).
**Fluxo principal:** Palestras → selecionar tema → escolher/cadastrar palestrante.
**Regras relacionadas:** RN-22, RN-23.
**Detalhe completo:** `conteudo-palestras.md`.

## UC-19 — Consultar estrutura hierárquica
**Ator:** Diocese ou Nacional.
**Fluxo principal:** Dioceses → visualizar tabela hierárquica.
**Regras relacionadas:** RN-04.
**Detalhe completo:** `hierarquia-territorial.md`.

## UC-20 — Registrar presença em encontro pós-encontro
**Ator:** perfil Pós-Encontro.
**Fluxo principal:** Painel Pós-Encontro → Registrar presença → marcar presentes.
**Regras relacionadas:** RN-19.
**Detalhe completo:** `pos-encontro.md` (fluxo dependente de telas ainda não totalmente especificadas).

## UC-21 — Trocar perfil de acesso
**Ator:** qualquer usuário autenticado.
**Fluxo principal:** abrir seletor de perfil na sidebar → escolher perfil → navegar ao painel inicial correspondente.
**Regras relacionadas:** RN-05.
**Detalhe completo:** `autenticacao-acesso.md`.

## UC-22 — Gerar relatório
**Ator:** perfis com item "Relatórios" no menu.
**Fluxo principal:** Relatórios → escolher tipo → gerar.
**Detalhe completo:** `sistema-relatorios-config.md`.
