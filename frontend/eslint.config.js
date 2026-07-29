// Regra de import-boundary (skills/padrao-componentes-react-tailwind.md, seção 3):
// domains/* pode importar de shared/*, nunca o contrário; domains/X nunca importa
// diretamente de domains/Y. Enforcement automático (eslint-plugin-boundaries) fica
// como próximo passo sugerido — não configurado nesta fase.
import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import tseslint from 'typescript-eslint'
import prettierConfig from 'eslint-config-prettier'

export default tseslint.config(
  { ignores: ['dist'] },
  {
    extends: [js.configs.recommended, ...tseslint.configs.recommended, prettierConfig],
    files: ['**/*.{ts,tsx}'],
    languageOptions: {
      ecmaVersion: 2023,
      globals: globals.browser,
    },
    plugins: {
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
    },
    rules: {
      ...reactHooks.configs.recommended.rules,
      'react-refresh/only-export-components': ['warn', { allowConstantExport: true }],
    },
  },
  // Os testes E2E (Playwright) rodam em Node, fora do React. As regras de hooks não se aplicam e
  // dão falso positivo no `use()` das fixtures do Playwright, que não é o hook `use` do React.
  {
    files: ['e2e/**/*.ts', 'playwright.config.ts'],
    languageOptions: { globals: globals.node },
    rules: {
      'react-hooks/rules-of-hooks': 'off',
      'react-refresh/only-export-components': 'off',
    },
  },
)
