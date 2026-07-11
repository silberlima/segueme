# Padrão de Componentes — React + Tailwind + DDD

Guia de arquitetura de front-end para a implementação real do Sistema Segue-me (e de qualquer outro projeto derivado desta documentação funcional). Define como organizar pastas, como separar responsabilidades dentro de um componente e como evitar misturar HTML, estilo e lógica/tipos no mesmo arquivo.

## 1. Objetivo
Garantir que qualquer desenvolvedor (ou o próprio Claude Code) implemente componentes de forma previsível, testável e organizada por domínio de negócio (DDD), usando **React + TypeScript + Tailwind CSS**, sem depender de convenções tácitas.

## 2. Princípios gerais
1. **Um arquivo, uma responsabilidade.** Nenhum arquivo deve conter ao mesmo tempo: marcação (JSX), estilo bruto (CSS/objetos de estilo) e definição de tipos/lógica de domínio complexa. Tailwind é a exceção permitida dentro do JSX (ver seção 4) — classes utilitárias não contam como "CSS separado", mas **CSS customizado, `@apply`, styled-components ou objetos `style={{ ... }}` complexos são proibidos** fora de casos excepcionais justificados em comentário.
2. **Organização por domínio (DDD), não por tipo técnico.** Não agrupar tudo em `components/`, `hooks/`, `types/` na raiz. Cada domínio de negócio (ex.: `pessoas`, `encontros`, `equipes`, `financeiro`) tem sua própria pasta, com subpastas técnicas dentro dela.
3. **Camadas explícitas dentro de um domínio:** apresentação (componentes React) → aplicação (hooks/casos de uso) → domínio (regras de negócio puras, tipos, entidades) → infraestrutura (chamadas a API/serviços externos). Uma camada não pula para outra sem passar pela imediatamente adjacente.
4. **Tipagem sempre explícita e isolada.** Nenhuma `interface`/`type` de domínio é declarada dentro de um arquivo `.tsx` de componente — vive em `*.types.ts`.
5. **Sem lógica de negócio dentro de componentes de apresentação.** Componentes de UI recebem dados e callbacks via props; cálculo, validação e orquestração vivem em hooks (`use*.ts`) ou serviços de domínio (`*.service.ts`).

## 3. Estrutura de pastas (DDD)

```
src/
  domains/
    pessoas/
      components/
        PersonCard/
          PersonCard.tsx          # apenas JSX + classes Tailwind
          PersonCard.types.ts     # props e tipos locais do componente
          index.ts                # barrel: export { PersonCard } from './PersonCard'
        PersonList/
          PersonList.tsx
          PersonList.types.ts
          index.ts
      hooks/
        usePersonList.ts          # estado, chamadas de dados, paginação, filtros
        usePersonForm.ts
      domain/
        person.entity.ts          # regras de negócio puras (situação, transições)
        person.types.ts           # Person, PersonStatus, etc.
      services/
        person.service.ts         # acesso a API/persistência (sem JSX, sem hooks de React)
      screens/
        PersonListScreen.tsx      # compõe hooks + componentes; é o "ponto de entrada" da rota
      index.ts                    # barrel público do domínio
    encontros/
      components/...
      hooks/...
      domain/...
      services/...
      screens/...
    equipes/
    montagem/
    inscricoes/
    financeiro/
    conteudo/
    setorial/
    sistema/
  shared/
    ui/                # componentes de design system reaproveitáveis entre domínios (Button, Badge, Modal, Table…)
      Button/
        Button.tsx
        Button.types.ts
        index.ts
    hooks/              # hooks genéricos, sem regra de negócio (useDebounce, useDisclosure…)
    utils/              # funções puras genéricas (formatDate, formatCurrency…)
    types/              # tipos verdadeiramente transversais (Pagination<T>, ApiError…)
  app/
    routes/             # definição de rotas, layout raiz
    providers/          # contexts globais (auth, tema, query client)
```

**Regra de dependência:** `domains/*` pode importar de `shared/*`, nunca o contrário. Um domínio não importa diretamente de dentro de outro domínio (`domains/pessoas` não importa de `domains/equipes/components`) — se dois domínios precisam compartilhar algo, esse algo sobe para `shared/` ou vira um evento/contrato explícito entre serviços.

## 4. Convenção de arquivos por componente

Todo componente de UI é uma **pasta**, nunca um arquivo solto, com no mínimo:

| Arquivo | Conteúdo | Proibido conter |
|---|---|---|
| `NomeDoComponente.tsx` | Somente JSX + classes Tailwind + chamada a hooks/props já prontos. Recebe dados via props. | Definição de tipos complexos, chamadas de API, lógica de cálculo/validação, CSS customizado |
| `NomeDoComponente.types.ts` | `interface NomeDoComponenteProps { ... }` e tipos auxiliares exclusivos deste componente | JSX, lógica |
| `index.ts` | Barrel: `export { NomeDoComponente } from './NomeDoComponente'` | Qualquer lógica |
| `useNomeDoComponente.ts` (quando houver estado/efeitos) | Hook com `useState`/`useEffect`/orquestração de serviços | JSX |
| `NomeDoComponente.test.tsx` (quando aplicável) | Testes | — |

**Exemplo mínimo — `shared/ui/Button`:**

`Button.types.ts`
```ts
export type ButtonVariant = 'primary' | 'secondary' | 'danger';

export interface ButtonProps {
  variant?: ButtonVariant;
  disabled?: boolean;
  onClick?: () => void;
  children: React.ReactNode;
}
```

`Button.tsx`
```tsx
import { ButtonProps } from './Button.types';

const variantClasses: Record<string, string> = {
  primary: 'bg-violet-600 text-white hover:bg-violet-700',
  secondary: 'bg-white border border-slate-200 text-slate-900 hover:border-slate-300',
  danger: 'bg-white border border-slate-200 text-rose-600 hover:border-rose-300',
};

export function Button({ variant = 'primary', disabled, onClick, children }: ButtonProps) {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      className={`h-10 px-4 rounded-lg text-sm font-semibold transition-colors disabled:opacity-50 ${variantClasses[variant]}`}
    >
      {children}
    </button>
  );
}
```

`index.ts`
```ts
export { Button } from './Button';
export type { ButtonProps, ButtonVariant } from './Button.types';
```

Note que **nenhuma classe utilitária Tailwind sai do `.tsx`** (isso é aceito, pois Tailwind é estilo *inline por design* e não CSS separado) e **nenhum tipo sai do `.types.ts`**.

## 5. Regras específicas de separação HTML / CSS / TypeScript

- **Proibido:** arquivos `.css`/`.scss` acoplados 1:1 a um componente com seletores que dependem de conhecer a estrutura do JSX (ex.: `.card .card-header h2`). Se um estilo não é exprimível em utilitário Tailwind, ele é candidato a virar um token no `tailwind.config` (cor, espaçamento, sombra), não um CSS solto.
- **Proibido:** `style={{ ... }}` para valores estáticos/de design. `style` inline em React só é aceito para um valor **verdadeiramente dinâmico em runtime** (ex.: `width: `${progress}%`` de uma barra de progresso) — nunca para cor, fonte ou espaçamento fixos.
- **Proibido:** misturar definição de `type`/`interface` de domínio dentro do mesmo arquivo de um componente de tela complexa. Toda tela (`screens/*.tsx`) importa seus tipos de `domain/*.types.ts` ou do `.types.ts` do componente.
- **Permitido e recomendado:** `@apply` **não é usado** neste padrão — preferir compor classes utilitárias diretamente no JSX ou extrair para uma função/`cva` (class-variance-authority) dentro do próprio arquivo do componente quando a combinação de variantes crescer (ver exemplo do `Button` acima com `variantClasses`).
- **Regra de ouro:** se ao abrir um arquivo você precisa rolar entre "aqui é HTML", "aqui é CSS" e "aqui é lógica de negócio/tipo", o arquivo está errado e deve ser dividido segundo a tabela da seção 4.

## 6. Nomenclatura
- Componentes: `PascalCase` (`PersonCard`, `EncounterWizard`).
- Hooks: `camelCase` iniciado por `use` (`usePersonList`).
- Tipos/Interfaces: `PascalCase`, sufixo `Props` para props de componente (`PersonCardProps`), sem prefixo `I`.
- Serviços: sufixo `.service.ts` (`person.service.ts`).
- Entidades/regra de domínio pura: sufixo `.entity.ts` (`person.entity.ts`) ou `.rules.ts` quando for só função de validação.
- Pastas de domínio: em português e no plural, alinhadas ao vocabulário já usado na documentação funcional (`pessoas`, `encontros`, `equipes`, `montagem`, `inscricoes`, `financeiro`, `setorial`, `conteudo`, `sistema`).

## 7. Exemplo de fluxo em camadas (domínio "montagem")
1. `domains/montagem/domain/suggestion.entity.ts` — função pura `calculateMatchScore(candidate, vacancy): number`, implementando os pesos 35/30/20/15% (RN-09 da documentação funcional). Sem import de React.
2. `domains/montagem/services/suggestion.service.ts` — busca candidatos e vagas via API; usa `calculateMatchScore` para ordenar.
3. `domains/montagem/hooks/useSuggestions.ts` — hook que chama o serviço, mantém estado de carregamento/erro, expõe `{ suggestions, isLoading, error, addToTeam }`.
4. `domains/montagem/components/SuggestionCard/SuggestionCard.tsx` — recebe um candidato via props e renderiza; nenhuma chamada de API aqui.
5. `domains/montagem/screens/SuggestionsScreen.tsx` — usa `useSuggestions()` e mapeia para `<SuggestionCard />`.

## 8. Enforcement sugerido (ferramental)
- **ESLint:** `eslint-plugin-boundaries` ou `eslint-plugin-import` com regras customizadas para bloquear import cruzado entre `domains/*` (exceto via `shared/`).
- **ESLint:** regra que proíbe `style={{` exceto quando o valor vem de uma variável (não de literal), e proíbe arquivos `.css`/`.scss` fora de `globals.css`/config do Tailwind.
- **Tailwind:** plugin `prettier-plugin-tailwindcss` para ordenar classes automaticamente e reduzir divergência de estilo entre PRs.
- **Revisão de PR (checklist mínimo):**
  - [ ] Nenhum `.tsx` contém `interface`/`type` de domínio declarado inline.
  - [ ] Nenhum `.tsx` contém `style={{...}}` com valor estático.
  - [ ] Nenhum import cruzado direto entre pastas de `domains/` diferentes.
  - [ ] Toda lógica de cálculo/regra de negócio está em `domain/` ou `services/`, não em componente.
  - [ ] Componente novo tem pasta própria com `index.ts` de barrel.

## 9. Relação com a documentação funcional
Este padrão é a contrapartida técnica dos documentos em `docs/*.md` (regras de negócio RN-xxx, requisitos RF-xxx, entidades). Cada entidade listada em `entidades-de-dominio.md` deve corresponder a uma pasta em `domains/<entidade-ou-agrupamento>/domain/` com seu respectivo `.entity.ts`/`.types.ts` — mantendo rastreabilidade entre requisito funcional e código.
